/*
 * Copyright 2022 Baptiste Candellier
 * Copyright 2019 Stephane Nicolas
 * Copyright 2019 Daniel Molinero Reguera
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package toothpick.compiler.memberinjector

import com.google.devtools.ksp.KspExperimental
import com.google.devtools.ksp.closestClassDeclaration
import com.google.devtools.ksp.isAnnotationPresent
import com.google.devtools.ksp.isConstructor
import com.google.devtools.ksp.isInternal
import com.google.devtools.ksp.isJavaPackagePrivate
import com.google.devtools.ksp.isPrivate
import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.symbol.FunctionKind
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.KSNode
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import toothpick.compiler.common.ToothpickProcessor
import toothpick.compiler.common.generators.error
import toothpick.compiler.common.generators.info
import toothpick.compiler.common.generators.targets.VariableInjectionTarget
import toothpick.compiler.common.generators.warn
import toothpick.compiler.memberinjector.generators.MemberInjectorGenerator
import toothpick.compiler.memberinjector.targets.MethodInjectionTarget
import javax.inject.Inject

/**
 * This processor's role is to create [toothpick.MemberInjector].
 *
 * We create factories in different situations:
 *
 *  * When a class `Foo` has an [javax.inject.Singleton] annotated field:
 *    * --> we create a MemberInjector to inject `Foo` instances.
 *  * When a class `Foo` has an [javax.inject.Singleton] method:
 *    * --> we create a MemberInjector to inject `Foo` instances.
 *
 */
@OptIn(KspExperimental::class)
class MemberInjectorProcessor(
    processorOptions: Map<String, String>,
    codeGenerator: CodeGenerator,
    logger: KSPLogger
) : ToothpickProcessor(
    processorOptions, codeGenerator, logger
) {
    override fun process(resolver: Resolver): List<KSAnnotated> {
        val injectedNodes: Sequence<KSAnnotated> =
            resolver.getSymbolsWithAnnotation(Inject::class.qualifiedName!!)

        val parentAndPropertiesToInject: Map<KSClassDeclaration, List<VariableInjectionTarget>> =
            injectedNodes
                .filterIsInstance<KSPropertyDeclaration>()
                .mapNotNull { property -> property.getParentClassOrNull()?.let { parent -> parent to property } }
                .filterNot { (parentClass, _) -> parentClass.isExcludedByFilters() }
                .filter { (_, property) -> property.isValidInjectAnnotatedProperty() }
                .groupBy(
                    { (parentClass, _) -> parentClass },
                    { (_, property) -> property.createFieldOrParamInjectionTarget() }
                )

        val parentAndMethodsToInject: Map<KSClassDeclaration, List<MethodInjectionTarget>> =
            injectedNodes
                .filterIsInstance<KSFunctionDeclaration>()
                .filter { function -> function.functionKind == FunctionKind.MEMBER }
                .filterNot { function -> function.isConstructor() }
                .mapNotNull { function -> function.getParentClassOrNull()?.let { parent -> parent to function } }
                .filterNot { (parentClass, _) -> parentClass.isExcludedByFilters() }
                .filter { (_, method) -> method.isValidInjectAnnotatedMethod() }
                .groupBy(
                    { (parentClass, _) -> parentClass },
                    { (_, method) ->
                        MethodInjectionTarget(
                            methodName = method.simpleName,
                            isOverride = method.isOverride(),
                            parameters = method.getParamInjectionTargetList()
                        )
                    }
                )

        // Generate member scopes
        parentAndPropertiesToInject.keys
            .plus(parentAndMethodsToInject.keys)
            .map { sourceClass ->
                MemberInjectorGenerator(
                    sourceClass = sourceClass,
                    superClassThatNeedsInjection = sourceClass.getMostDirectSuperClassWithInjectedMembers(onlyParents = true),
                    variableInjectionTargetList = parentAndPropertiesToInject[sourceClass],
                    methodInjectionTargetList = parentAndMethodsToInject[sourceClass]
                )
            }
            .forEach { generator ->
                writeToFile(
                    tpCodeGenerator = generator,
                    fileDescription = "MemberInjector for type ${generator.sourceClassName}"
                )

                if (options.verboseLogging) {
                    logger.info(
                        "%s generated class %s",
                        generator.sourceClassName.toString(),
                        generator.generatedClassName.toString()
                    )
                }
            }

        return emptyList()
    }

    private fun KSPropertyDeclaration.createFieldOrParamInjectionTarget(): VariableInjectionTarget =
        VariableInjectionTarget.create(this, logger)

    /**
     * Checks if a given method overrides an [Inject]-annotated method.
     * @receiver The method to check.
     */
    private fun KSFunctionDeclaration.isOverride(): Boolean {
        return findOverridee()?.isAnnotationPresent(Inject::class) == true
    }

    private fun KSPropertyDeclaration.isValidInjectAnnotatedProperty(): Boolean {
        // Verify modifiers.
        if (isPrivate()) {
            logger.error(
                this,
                "@Inject-annotated fields must not be private: %s",
                qualifiedName?.asString()
            )
            return false
        }

        return type.resolve().isValidInjectedType(
            node = this,
            qualifiedName = qualifiedName?.asString()
        )
    }

    private fun KSFunctionDeclaration.isValidInjectAnnotatedMethod(): Boolean {
        if (isPrivate()) {
            logger.error(
                this,
                "@Inject-annotated methods must not be private: %s",
                qualifiedName?.asString()
            )
            return false
        }

        val parentClass = closestClassDeclaration()
        if (parentClass == null) {
            logger.error(
                this,
                "@Inject-annotated function %s must be part of a class.",
                qualifiedName?.asString()
            )
            return false
        }

        val areParametersValid =
            parameters
                .map { param -> param.type.resolve() }
                .all { type ->
                    type.isValidInjectedType(
                        node = this,
                        qualifiedName = qualifiedName?.asString()
                    )
                }

        if (!areParametersValid) return false

        if (!isJavaPackagePrivate() && !isInternal()) {
            if (!hasWarningSuppressed(SUPPRESS_WARNING_ANNOTATION_VISIBLE_VALUE)) {
                crashOrWarnWhenMethodIsNotPackageOrInternal(
                    this,
                    "@Inject-annotated methods should have package or internal visibility: ${qualifiedName?.asString()}",
                )
            }
        }

        return true
    }

    private fun crashOrWarnWhenMethodIsNotPackageOrInternal(node: KSNode, message: String) {
        if (options.crashWhenInjectedMethodIsNotPackageVisible) logger.error(node, message)
        else logger.warn(node, message)
    }

    companion object {

        /**
         * Custom value for [SuppressWarnings]. Suppresses warning when an injected
         * method is not package-private visible.
         */
        private const val SUPPRESS_WARNING_ANNOTATION_VISIBLE_VALUE = "visible"
    }
}
