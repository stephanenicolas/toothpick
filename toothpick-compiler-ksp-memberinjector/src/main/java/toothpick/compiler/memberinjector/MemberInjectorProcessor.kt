/*
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

import com.google.devtools.ksp.isConstructor
import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.symbol.*
import toothpick.compiler.common.ToothpickProcessor
import toothpick.compiler.common.generators.info
import toothpick.compiler.common.generators.targets.FieldInjectionTarget
import toothpick.compiler.memberinjector.generators.MemberInjectorGenerator
import toothpick.compiler.memberinjector.targets.MethodInjectionTarget
import javax.inject.Inject

/**
 * This processor's role is to create [MemberInjector]. We create factories in different
 * situations :
 *
 *
 *  * When a class `Foo` has an [javax.inject.Singleton] annotated field : <br></br>
 * --> we create a MemberInjector to inject `Foo` instances.
 *  * When a class `Foo` has an [javax.inject.Singleton] method : <br></br>
 * --> we create a MemberInjector to inject `Foo` instances.
 *
 */
class MemberInjectorProcessor(
    processorOptions: Map<String, String>,
    codeGenerator: CodeGenerator,
    logger: KSPLogger
) : ToothpickProcessor(
    processorOptions, codeGenerator, logger
) {
    override fun process(resolver: Resolver): List<KSAnnotated> {
        val injectedElements: Sequence<KSAnnotated> =
            resolver.getSymbolsWithAnnotation(Inject::class.qualifiedName!!)

        val typeElementToFieldInjectorTargetList: Map<KSClassDeclaration, List<FieldInjectionTarget>> =
            injectedElements
                .filterIsInstance<KSPropertyDeclaration>()
                .mapNotNull { property -> property.getParentClassOrNull()?.let { parent -> parent to property } }
                .filterNot { (parentClass, _) -> parentClass.isExcludedByFilters() }
                .filter { (_, property) -> property.isValidInjectAnnotatedProperty() }
                .groupBy(
                    { (parentClass, _) -> parentClass },
                    { (_, property) -> property.createFieldOrParamInjectionTarget() }
                )

        val typeElementToMethodInjectorTargetList: Map<KSClassDeclaration, List<MethodInjectionTarget>> =
            injectedElements
                .filterIsInstance<KSFunctionDeclaration>()
                .filter { function -> function.functionKind == FunctionKind.MEMBER }
                .filterNot { function -> function.isConstructor() }
                .mapNotNull { function -> function.getParentClassOrNull()?.let { parent -> parent to function } }
                .filterNot { (parentClass, _) -> parentClass.isExcludedByFilters() }
                .filter { (_, method) -> method.isValidInjectAnnotatedMethod() }
                .groupBy(
                    { (parentClass, _) -> parentClass },
                    { (_, method) -> method.createMethodInjectionTarget() }
                )

        // Generate member scopes
        typeElementToFieldInjectorTargetList.keys
            .plus(typeElementToMethodInjectorTargetList.keys)
            .map { sourceClass ->
                MemberInjectorGenerator(
                    sourceClass = sourceClass,
                    superClassThatNeedsInjection = sourceClass.getMostDirectSuperClassWithInjectedMembers(),
                    fieldInjectionTargetList = typeElementToFieldInjectorTargetList[sourceClass],
                    methodInjectionTargetList = typeElementToMethodInjectorTargetList[sourceClass]
                )
            }
            .forEach { generator ->
                writeToFile(
                    tpCodeGenerator = generator,
                    fileDescription = "MemberInjector for type ${generator.sourceClassName}"
                )

                if (options.debugLogOriginatingElements) {
                    logger.info(
                        "%s generated class %s",
                        generator.sourceClassName.toString(),
                        generator.generatedClassName.toString()
                    )
                }
            }

        return emptyList()
    }

    private fun KSFunctionDeclaration.createMethodInjectionTarget(): MethodInjectionTarget {
        return MethodInjectionTarget(
            methodName = simpleName,
            isOverride = isOverride(),
            parameters = getParamInjectionTargetList()
        )
    }
}
