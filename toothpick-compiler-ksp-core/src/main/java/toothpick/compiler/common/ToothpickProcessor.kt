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
package toothpick.compiler.common

import com.google.devtools.ksp.KspExperimental
import com.google.devtools.ksp.closestClassDeclaration
import com.google.devtools.ksp.isAnnotationPresent
import com.google.devtools.ksp.isConstructor
import com.google.devtools.ksp.isPrivate
import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.symbol.ClassKind
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.KSNode
import com.google.devtools.ksp.symbol.KSType
import com.google.devtools.ksp.symbol.Variance
import com.squareup.kotlinpoet.ksp.KotlinPoetKspPreview
import com.squareup.kotlinpoet.ksp.writeTo
import toothpick.compiler.common.generators.TPCodeGenerator
import toothpick.compiler.common.generators.error
import toothpick.compiler.common.generators.exception
import toothpick.compiler.common.generators.getAnnotationsByType
import toothpick.compiler.common.generators.targets.VariableInjectionTarget
import toothpick.compiler.common.generators.warn
import java.io.IOException
import javax.inject.Inject

@OptIn(KspExperimental::class, KotlinPoetKspPreview::class)
abstract class ToothpickProcessor(
    processorOptions: Map<String, String>,
    private val codeGenerator: CodeGenerator,
    protected val logger: KSPLogger
) : SymbolProcessor {

    protected val options = processorOptions.readOptions()

    protected fun writeToFile(tpCodeGenerator: TPCodeGenerator, fileDescription: String): Boolean {
        return try {
            tpCodeGenerator
                .brewCode()
                .writeTo(codeGenerator, aggregating = false)
            true
        } catch (e: IOException) {
            logger.exception(e, "Error while writing file %s", fileDescription)
            false
        }
    }

    protected fun KSType.isValidInjectedType(node: KSNode, qualifiedName: String?): Boolean {
        return if (!isValidInjectedClassKind()) false
        else !isProviderOrLazy() || isValidProviderOrLazy(node, qualifiedName)
    }

    private fun KSType.isValidInjectedClassKind(): Boolean =
        (declaration as? KSClassDeclaration)?.classKind in validInjectableTypes

    private fun KSType.isValidProviderOrLazy(node: KSNode, qualifiedName: String?): Boolean {
        // e.g. Provider<Foo<String>>
        // -> Foo<String>
        val firstArgumentType = arguments.firstOrNull()?.type?.resolve()

        if (firstArgumentType == null ||
            firstArgumentType.declaration.qualifiedName?.asString() == Any::class.qualifiedName
        ) {
            logger.error(
                node,
                "Type of %s is not a valid %s.",
                qualifiedName,
                declaration.qualifiedName?.asString()
            )
        }

        // -> String
        val firstArgumentArguments = firstArgumentType?.arguments ?: return true
        if (firstArgumentArguments.isEmpty()) return true

        val firstArgumentArgument = firstArgumentArguments.first()
        val firstArgumentArgumentType = firstArgumentArgument.type?.resolve()?.declaration?.qualifiedName?.asString()

        val isArgumentStar = firstArgumentArgument.variance == Variance.STAR
        val isArgumentAny = firstArgumentArgument.variance == Variance.INVARIANT && firstArgumentArgumentType == Any::class.qualifiedName
        val areValidArguments = firstArgumentArguments.size <= 1 && (isArgumentStar || isArgumentAny)

        if (!areValidArguments) {
            logger.error(
                node,
                "Type of %s is not a valid Lazy/Provider. Lazy/Provider cannot be used on generic types.",
                qualifiedName
            )
            return false
        }

        return true
    }

    protected fun KSFunctionDeclaration.getParamInjectionTargetList(): List<VariableInjectionTarget> =
        parameters.map { param -> VariableInjectionTarget.create(param, logger) }

    protected fun KSDeclaration.isExcludedByFilters(): Boolean {
        val qualifiedName = qualifiedName?.asString() ?: return true
        return options.excludes
            .map { exclude -> exclude.toRegex() }
            .any { exclude -> qualifiedName.matches(exclude) }
            .also { isExcluded ->
                if (isExcluded) {
                    logger.warn(
                        this,
                        "Class %s was excluded by filters set at the annotation processor level. " +
                            "No factory will be generated by Toothpick.",
                        qualifiedName
                    )
                }
            }
    }

    protected fun KSClassDeclaration.getMostDirectSuperClassWithInjectedMembers(onlyParents: Boolean): KSClassDeclaration? {
        if (!onlyParents && hasInjectAnnotatedMembers()) return this

        val parentClass: KSClassDeclaration? = superTypes
            .map { superType -> superType.resolve().declaration }
            .filterIsInstance<KSClassDeclaration>()
            .filter { superClass -> superClass.classKind == ClassKind.CLASS }
            .firstOrNull()

        return parentClass?.getMostDirectSuperClassWithInjectedMembers(onlyParents = false)
    }

    private fun KSClassDeclaration.hasInjectAnnotatedMembers(): Boolean {
        // Ignore overridden members. They will be injected by the parent MemberInjector.
        return getAllFunctions().filter { function -> function.findOverridee() == null && !function.isConstructor() }
            .plus(getAllProperties().filter { property -> property.findOverridee() == null })
            .any { member -> member.isAnnotationPresent(Inject::class) }
    }

    protected fun KSDeclaration.getParentClassOrNull(): KSClassDeclaration? {
        val parentClass = closestClassDeclaration()
        if (parentClass == null) {
            logger.error(
                this,
                "@Inject-annotated property %s must be part of a class.",
                qualifiedName?.asString()
            )
            return null
        }

        if (parentClass.isPrivate()) {
            logger.error(
                this,
                "Parent class of @Inject-annotated class %s must not be private.",
                qualifiedName?.asString()
            )
            return null
        }

        return parentClass
    }

    /**
     * Checks if this node has a @SuppressWarning([warningSuppressString]) annotation.
     *
     * @receiver the node to check if the warning is suppressed.
     * @param warningSuppressString the value of the SuppressWarning annotation.
     * @return true is the injectable warning is suppressed, false otherwise.
     */
    protected fun KSAnnotated.hasWarningSuppressed(warningSuppressString: String): Boolean {
        val kotlinAnnotations =
            getAnnotationsByType<Suppress>()
                .flatMap { annotation ->
                    annotation.arguments
                        .flatMap { arg -> arg.value as List<*> }
                        .filterIsInstance<String>()
                }

        val javaAnnotations =
            getAnnotationsByType<SuppressWarnings>()
                .flatMap { annotation ->
                    annotation.arguments.map { arg -> arg.value as String }
                }

        return (kotlinAnnotations + javaAnnotations)
            .any { value -> value.equals(warningSuppressString, ignoreCase = true) }
    }

    private fun KSType.isProviderOrLazy(): Boolean {
        val qualifiedName = declaration.qualifiedName?.asString()
        return qualifiedName == javax.inject.Provider::class.qualifiedName ||
            qualifiedName == toothpick.Lazy::class.qualifiedName
    }

    private val validInjectableTypes = arrayOf(ClassKind.CLASS, ClassKind.INTERFACE, ClassKind.ENUM_CLASS)
}
