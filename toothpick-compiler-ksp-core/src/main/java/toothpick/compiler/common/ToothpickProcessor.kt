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
import com.google.devtools.ksp.getAnnotationsByType
import com.google.devtools.ksp.isAnnotationPresent
import com.google.devtools.ksp.isConstructor
import com.google.devtools.ksp.isInternal
import com.google.devtools.ksp.isJavaPackagePrivate
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
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.google.devtools.ksp.symbol.KSValueParameter
import com.google.devtools.ksp.symbol.Modifier
import com.google.devtools.ksp.symbol.Variance
import com.squareup.kotlinpoet.ksp.KotlinPoetKspPreview
import com.squareup.kotlinpoet.ksp.writeTo
import toothpick.compiler.common.generators.TPCodeGenerator
import toothpick.compiler.common.generators.error
import toothpick.compiler.common.generators.hasAnnotation
import toothpick.compiler.common.generators.targets.FieldInjectionTarget
import toothpick.compiler.common.generators.targets.ParamInjectionTarget
import toothpick.compiler.common.generators.warn
import java.io.IOException
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Qualifier

/** Base processor class.  */
@OptIn(KspExperimental::class)
abstract class ToothpickProcessor(
    processorOptions: Map<String, String>,
    private val codeGenerator: CodeGenerator,
    protected val logger: KSPLogger
) : SymbolProcessor {

    protected val options = processorOptions.readOptions()

    @OptIn(KotlinPoetKspPreview::class)
    protected fun writeToFile(tpCodeGenerator: TPCodeGenerator, fileDescription: String): Boolean {
        return try {
            tpCodeGenerator
                .brewCode()
                .writeTo(codeGenerator, aggregating = false)
            true
        } catch (e: IOException) {
            logger.error("Error writing %s file: %s", fileDescription, e.message)
            false
        }
    }

    protected fun KSDeclaration.hasParentDeclaration(): Boolean {
        if (parentDeclaration == null) {
            logger.error(
                this,
                "Top-level element %s cannot be injected. Remove @Inject annotation or move this element to a Class.",
                qualifiedName?.asString()
            )
            return false
        }
        return true
    }

    protected fun KSPropertyDeclaration.isValidInjectAnnotatedProperty(): Boolean {
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

    protected fun KSValueParameter.isValidInjectAnnotatedParameter(): Boolean {
        val parentClass = (parent as? KSDeclaration)?.closestClassDeclaration()
        if (parentClass == null) {
            logger.error(
                this,
                "@Inject-annotated field %s must be part of a class.",
                name?.asString()
            )
            return false
        }

        val parentFun = parent as? KSFunctionDeclaration
        if (parentFun == null) {
            logger.error(
                this,
                "@Inject-annotated field %s must be part of a function.",
                name?.asString()
            )
            return false
        }

        return type.resolve().isValidInjectedType(
            node = this,
            qualifiedName = "${parentFun.qualifiedName?.asString()}#$name"
        )
    }

    protected fun KSFunctionDeclaration.isValidInjectAnnotatedMethod(): Boolean {
        // Verify modifiers.
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
                crashOrWarnWhenMethodIsNotPackageVisible(
                    this,
                    "@Inject-annotated methods should have package/internal visibility: ${qualifiedName?.asString()}",
                )
            }
        }

        return true
    }

    protected fun KSType.isValidInjectedType(node: KSNode, qualifiedName: String?): Boolean {
        return if (!isValidInjectedElementKind()) false
        else !isProviderOrLazy() || isValidProviderOrLazy(node, qualifiedName)
    }

    private fun KSType.isValidInjectedElementKind(): Boolean =
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
                (node as KSDeclaration).qualifiedName?.asString(),
                declaration.qualifiedName?.asString()
            )
        }

        // -> String
        val firstArgumentArguments = firstArgumentType?.arguments ?: return true
        if (firstArgumentArguments.isEmpty()) return true

        val firstArgumentArgument = firstArgumentArguments.first()
        val firstArgumentArgumentType = firstArgumentArgument.type?.resolve()?.declaration?.qualifiedName?.asString()

        val areValidArguments =
            firstArgumentArguments.size <= 1 &&
                (
                    firstArgumentArgument.variance == Variance.STAR ||
                        (firstArgumentArgument.variance == Variance.INVARIANT && firstArgumentArgumentType == Any::class.qualifiedName)
                    )

        if (!areValidArguments) {
            logger.error(
                node,
                "Lazy/Provider is not valid in %s. Lazy/Provider cannot be used on generic types.",
                qualifiedName
            )
            return false
        }

        return true
    }

    protected fun KSFunctionDeclaration.getParamInjectionTargetList(): List<ParamInjectionTarget> =
        parameters.map { variable ->
            val type = variable.type.resolve()
            FieldInjectionTarget(
                memberClass = type.declaration as KSClassDeclaration,
                memberName = variable.name!!,
                kind = type.getParamInjectionTargetKind(),
                kindParamClass = type.getInjectedType(),
                qualifierName = findQualifierName()
            )
        }

    protected fun KSPropertyDeclaration.createFieldOrParamInjectionTarget(): FieldInjectionTarget {
        val type = type.resolve()
        return FieldInjectionTarget(
            memberClass = type.getInjectedType().declaration as KSClassDeclaration,
            memberName = simpleName,
            kind = type.getParamInjectionTargetKind(),
            kindParamClass = type.getInjectedType(),
            qualifierName = findQualifierName()
        )
    }

    /**
     * Retrieves the type of a field or param. The type can be the type of the parameter in the java
     * way (e.g. [b: B], type is [B]; but it can also be the type of a [toothpick.Lazy] or
     * [javax.inject.Provider] (e.g. [Lazy<B>], type is [B] not [Lazy]).
     *
     * @receiver the field or variable element type
     * @return the type has defined above.
     */
    private fun KSType.getInjectedType(): KSType {
        return when (getParamInjectionTargetKind()) {
            ParamInjectionTarget.Kind.INSTANCE -> starProjection()
            else -> arguments.first().type!!.resolve().starProjection()
        }
    }

    protected fun KSDeclaration.isExcludedByFilters(): Boolean {
        val typeElementName = qualifiedName.toString()
        return options.excludes
            .map { exclude -> exclude.toRegex() }
            .any { exclude -> typeElementName.matches(exclude) }
            .also { isExcluded ->
                if (isExcluded) {
                    logger.warn(
                        this,
                        "The class %s was excluded by filters set at the annotation processor level. " +
                            "No factory will be generated by toothpick.",
                        qualifiedName
                    )
                }
            }
    }

    // overrides are simpler in this case as methods can only be package or protected.
    // a method with the same name in the type hierarchy would necessarily mean that
    // the {@code methodElement} would be an override of this method.
    protected fun KSFunctionDeclaration.isOverride(): Boolean {
        return findOverridee()?.isAnnotationPresent(Inject::class) == true
    }

    protected fun KSClassDeclaration.getMostDirectSuperClassWithInjectedMembers(onlyParents: Boolean): KSClassDeclaration? {
        if (!onlyParents && hasInjectAnnotatedMembers()) return this

        val parentClass: KSClassDeclaration? = superTypes
            .map { superType -> superType.resolve().declaration }
            .filterIsInstance<KSClassDeclaration>()
            .filter { it.classKind == ClassKind.CLASS }
            .firstOrNull()

        return parentClass?.getMostDirectSuperClassWithInjectedMembers(onlyParents = false)
    }

    private fun KSClassDeclaration.hasInjectAnnotatedMembers(): Boolean {
        val members: Sequence<KSAnnotated> = getAllFunctions() + getAllProperties()
        return members
            .filterNot { function -> function is KSFunctionDeclaration && function.isConstructor() }
            .any { member -> member.isAnnotationPresent(Inject::class) }
    }

    protected fun KSClassDeclaration.isNonStaticInnerClass(): Boolean {
        if (parentDeclaration is KSClassDeclaration && !modifiers.contains(Modifier.JAVA_STATIC)) {
            logger.error(
                this,
                "Class %s is a non static inner class. @Inject constructors are not allowed in non static inner classes.",
                qualifiedName?.asString()
            )
            return true
        }
        return false
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
                "@Injected %s; the parent class must not be private.",
                qualifiedName?.asString()
            )
            return null
        }

        return parentClass
    }

    /**
     * Checks if `element` has a @SuppressWarning("`warningSuppressString`") annotation.
     *
     * @param element the element to check if the warning is suppressed.
     * @param warningSuppressString the value of the SuppressWarning annotation.
     * @return true is the injectable warning is suppressed, false otherwise.
     */
    protected fun KSAnnotated.hasWarningSuppressed(warningSuppressString: String): Boolean {
        return hasAnnotation<SuppressWarnings> { annotation ->
            annotation.arguments
                .map { it.value as String }
                .any { value -> value.equals(warningSuppressString, ignoreCase = true) }
        }
    }

    /**
     * Lookup both [javax.inject.Qualifier] and [javax.inject.Named] to provide the name
     * of an injection.
     *
     * @param element the element for which a qualifier is to be found.
     * @return the name of this element or null if it has no qualifier annotations.
     */
    private fun KSAnnotated.findQualifierName(): String? {
        val qualifierAnnotationNames = annotations
            .mapNotNull { annotation ->
                val annotationClass = annotation.annotationType.resolve().declaration
                val annotationClassName = annotationClass.qualifiedName?.asString()
                if (annotationClass.isAnnotationPresent(Qualifier::class)) {
                    annotationClassName.takeIf { it != Named::class.qualifiedName }
                } else null
            }

        val namedValues = getAnnotationsByType(Named::class)
            .map { annotation -> annotation.value }

        val allNames = qualifierAnnotationNames + namedValues

        if (allNames.count() > 1) {
            logger.error(this, "Only one javax.inject.Qualifier annotation is allowed to name injections.")
        }

        return allNames.firstOrNull()
    }

    private fun KSType.isProviderOrLazy(): Boolean =
        getParamInjectionTargetKind() in arrayOf(
            ParamInjectionTarget.Kind.PROVIDER,
            ParamInjectionTarget.Kind.LAZY
        )

    private fun KSType.getParamInjectionTargetKind(): ParamInjectionTarget.Kind =
        when (declaration.qualifiedName?.asString()) {
            javax.inject.Provider::class.qualifiedName -> ParamInjectionTarget.Kind.PROVIDER
            toothpick.Lazy::class.qualifiedName -> ParamInjectionTarget.Kind.LAZY
            else -> ParamInjectionTarget.Kind.INSTANCE
        }

    private val validInjectableTypes = arrayOf(ClassKind.CLASS, ClassKind.INTERFACE, ClassKind.ENUM_CLASS)

    private fun crashOrWarnWhenMethodIsNotPackageVisible(element: KSNode, message: String) {
        if (options.crashWhenInjectedMethodIsNotPackageVisible) logger.error(element, message)
        else logger.warn(element, message)
    }

    companion object {
        /** Allows to suppress warning when an injected method is not package-private visible.  */
        private const val SUPPRESS_WARNING_ANNOTATION_VISIBLE_VALUE = "visible"
    }
}
