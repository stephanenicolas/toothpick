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
package toothpick.compiler.common

import org.jetbrains.annotations.TestOnly
import toothpick.compiler.common.generators.CodeGenerator
import toothpick.compiler.common.generators.targets.ParamInjectionTarget
import toothpick.compiler.memberinjector.targets.FieldInjectionTarget
import java.io.IOException
import java.io.Writer
import javax.annotation.processing.AbstractProcessor
import javax.annotation.processing.Filer
import javax.annotation.processing.ProcessingEnvironment
import javax.inject.Inject
import javax.inject.Qualifier
import javax.lang.model.SourceVersion
import javax.lang.model.element.*
import javax.lang.model.type.DeclaredType
import javax.lang.model.type.TypeKind
import javax.lang.model.type.TypeMirror
import javax.lang.model.util.Elements
import javax.lang.model.util.Types
import javax.tools.Diagnostic

/** Base processor class.  */
abstract class ToothpickProcessor : AbstractProcessor() {

    private lateinit var elementUtils: Elements
    protected lateinit var typeUtils: Types
    private lateinit var filer: Filer

    protected var optionsOverride: ToothpickProcessorOptions? = null
    protected lateinit var options: ToothpickProcessorOptions

    @Synchronized
    override fun init(processingEnv: ProcessingEnvironment) {
        super.init(processingEnv)
        options = optionsOverride ?: processingEnv.readOptions()
        elementUtils = processingEnv.elementUtils
        typeUtils = processingEnv.typeUtils
        filer = processingEnv.filer
    }

    override fun getSupportedSourceVersion(): SourceVersion = SourceVersion.latestSupported()

    protected fun writeToFile(
        codeGenerator: CodeGenerator,
        fileDescription: String?,
        originatingElement: Element?
    ): Boolean {
        var writer: Writer? = null
        var success = true
        try {
            val jfo = filer.createSourceFile(codeGenerator.fqcn, originatingElement)
            writer = jfo.openWriter()
            writer.write(codeGenerator.brewJava())
        } catch (e: IOException) {
            error("Error writing %s file: %s", fileDescription, e.message)
            success = false
        } finally {
            try {
                writer?.close()
            } catch (e: IOException) {
                error("Error closing %s file: %s", fileDescription, e.message)
                success = false
            }
        }
        return success
    }

    protected fun error(message: String, vararg args: Any?) {
        processingEnv.messager.printMessage(Diagnostic.Kind.ERROR, String.format(message, *args))
    }

    protected fun error(element: Element?, message: String, vararg args: Any?) {
        processingEnv.messager.printMessage(Diagnostic.Kind.ERROR, String.format(message, *args), element)
    }

    protected fun warning(element: Element?, message: String, vararg args: Any?) {
        processingEnv.messager.printMessage(Diagnostic.Kind.WARNING, String.format(message, *args), element)
    }

    protected fun warning(message: String, vararg args: Any?) {
        processingEnv.messager.printMessage(Diagnostic.Kind.WARNING, String.format(message, *args))
    }

    private fun crashOrWarnWhenMethodIsNotPackageVisible(element: Element, message: String) {
        if (options.crashWhenInjectedMethodIsNotPackageVisible) error(element, message)
        else warning(element, message)
    }

    protected fun isValidInjectAnnotatedFieldOrParameter(variableElement: VariableElement): Boolean {
        val enclosingElement = variableElement.enclosingElement as TypeElement

        // Verify modifiers.
        val modifiers = variableElement.modifiers
        if (modifiers.contains(Modifier.PRIVATE)) {
            error(
                variableElement,
                "@Inject annotated fields must be non private : %s#%s",
                enclosingElement.qualifiedName,
                variableElement.simpleName
            )
            return false
        }

        // Verify parentScope modifiers.
        val parentModifiers = enclosingElement.modifiers
        if (parentModifiers.contains(Modifier.PRIVATE)) {
            error(
                variableElement,
                "@Injected fields in class %s. The class must be non private.",
                enclosingElement.simpleName
            )
            return false
        }
        return isValidInjectedType(variableElement)
    }

    protected fun isValidInjectAnnotatedMethod(methodElement: ExecutableElement): Boolean {
        val enclosingElement = methodElement.enclosingElement as TypeElement

        // Verify modifiers.
        val modifiers = methodElement.modifiers
        if (modifiers.contains(Modifier.PRIVATE)) {
            error(
                methodElement,
                "@Inject annotated methods must not be private : %s#%s",
                enclosingElement.qualifiedName,
                methodElement.simpleName
            )
            return false
        }

        // Verify parentScope modifiers.
        val parentModifiers = enclosingElement.modifiers
        if (parentModifiers.contains(Modifier.PRIVATE)) {
            error(
                methodElement,
                "@Injected fields in class %s. The class must be non private.",
                enclosingElement.simpleName
            )
            return false
        }

        if (methodElement.parameters.any { paramElement -> !isValidInjectedType(paramElement) }) {
            return false
        }

        if (modifiers.contains(Modifier.PUBLIC) || modifiers.contains(Modifier.PROTECTED)) {
            if (!hasWarningSuppressed(methodElement, SUPPRESS_WARNING_ANNOTATION_VISIBLE_VALUE)) {
                crashOrWarnWhenMethodIsNotPackageVisible(
                    methodElement, String.format(
                        "@Inject annotated methods should have package visibility: %s#%s",
                        enclosingElement.qualifiedName, methodElement.simpleName
                    )
                )
            }
        }
        return true
    }

    protected fun isValidInjectedType(injectedTypeElement: VariableElement): Boolean {
        return if (!isValidInjectedElementKind(injectedTypeElement)) false
        else !isProviderOrLazy(injectedTypeElement) || isValidProviderOrLazy(injectedTypeElement)
    }

    private fun isValidInjectedElementKind(injectedTypeElement: VariableElement): Boolean {
        val typeElement: Element? = typeUtils.asElement(injectedTypeElement.asType())

        // typeElement can be null for primitives.
        // https://github.com/stephanenicolas/toothpick/issues/261
        if (typeElement == null
            || typeElement.kind != ElementKind.CLASS
            && typeElement.kind != ElementKind.INTERFACE
            && typeElement.kind != ElementKind.ENUM
        ) {
            // find the class containing the element
            // the element can be a field or a parameter
            var enclosingElement = injectedTypeElement.enclosingElement
            val typeName = typeElement?.toString() ?: injectedTypeElement.asType().toString()

            when (enclosingElement) {
                is TypeElement -> {
                    error(
                        injectedTypeElement,
                        "Field %s#%s is of type %s which is not supported by Toothpick.",
                        enclosingElement.qualifiedName,
                        injectedTypeElement.simpleName,
                        typeName
                    )
                }
                else -> {
                    val methodOrConstructorElement = enclosingElement
                    enclosingElement = enclosingElement.enclosingElement
                    error(
                        injectedTypeElement,
                        "Parameter %s in method/constructor %s#%s is of type %s which is not supported by Toothpick.",
                        injectedTypeElement.simpleName,
                        (enclosingElement as TypeElement).qualifiedName,
                        methodOrConstructorElement.simpleName,
                        typeName
                    )
                }
            }
            return false
        }
        return true
    }

    private fun isValidProviderOrLazy(element: Element): Boolean {
        val declaredType = element.asType() as DeclaredType

        // Contains type parameter
        if (declaredType.typeArguments.isEmpty()) {
            val enclosingElement = element.enclosingElement
            if (enclosingElement is TypeElement) {
                error(
                    element,
                    "Field %s#%s is not a valid %s.",
                    enclosingElement.qualifiedName,
                    element.simpleName,
                    declaredType
                )
            } else {
                error(
                    element,
                    "Parameter %s in method/constructor %s#%s is not a valid %s.",
                    element.simpleName,
                    (enclosingElement.enclosingElement as TypeElement).qualifiedName,
                    enclosingElement.simpleName,
                    declaredType
                )
            }
            return false
        }
        val firstParameterTypeMirror = declaredType.typeArguments[0]
        if (firstParameterTypeMirror.kind == TypeKind.DECLARED) {
            val size = (firstParameterTypeMirror as DeclaredType).typeArguments.size
            if (size != 0) {
                val enclosingElement = element.enclosingElement
                error(
                    element,
                    "Lazy/Provider %s is not a valid in %s. Lazy/Provider cannot be used on generic types.",
                    element.simpleName,
                    enclosingElement.simpleName
                )
                return false
            }
        }
        return true
    }

    protected fun getParamInjectionTargetList(executableElement: ExecutableElement): List<ParamInjectionTarget> {
        return executableElement.parameters.map { variableElement ->
            createFieldOrParamInjectionTarget(variableElement)
        }
    }

    protected fun getExceptionTypes(methodElement: ExecutableElement): List<TypeElement> {
        val exceptionClassNames: MutableList<TypeElement> = ArrayList()
        for (thrownTypeMirror in methodElement.thrownTypes) {
            val thrownDeclaredType = thrownTypeMirror as DeclaredType
            val thrownType = thrownDeclaredType.asElement() as TypeElement
            exceptionClassNames.add(thrownType)
        }
        return exceptionClassNames
    }

    protected fun createFieldOrParamInjectionTarget(
        variableElement: VariableElement
    ): FieldInjectionTarget {
        val memberTypeElement = typeUtils.asElement(variableElement.asType()) as TypeElement
        val memberName = variableElement.simpleName.toString()
        val kind = getParamInjectionTargetKind(variableElement)
        val kindParameterTypeElement = getInjectedType(variableElement)
        val name = findQualifierName(variableElement)
        return FieldInjectionTarget(
            memberClass = memberTypeElement,
            memberName = memberName,
            kind = kind,
            kindParamClass = kindParameterTypeElement,
            name = name
        )
    }

    /**
     * Retrieves the type of a field or param. The type can be the type of the parameter in the java
     * way (e.g. `B b`, type is `B`); but it can also be the type of a [ ] or [javax.inject.Provider] (e.g. `Lazy&lt;B&gt; b`, type is `B` not `Lazy`).
     *
     * @param variableElement the field or variable element. NOT his type !
     * @return the type has defined above.
     */
    private fun getInjectedType(variableElement: VariableElement): TypeElement {
        return when (getParamInjectionTargetKind(variableElement)) {
            ParamInjectionTarget.Kind.INSTANCE -> {
                typeUtils.asElement(typeUtils.erasure(variableElement.asType())) as TypeElement
            }
            else -> getKindParameter(variableElement)
        }
    }

    protected fun isExcludedByFilters(typeElement: TypeElement): Boolean {
        val typeElementName = typeElement.qualifiedName.toString()
        for (exclude in options.excludes.toTypedArray()) {
            val regEx = exclude.toRegex()
            if (typeElementName.matches(regEx)) {
                warning(
                    typeElement,
                    "The class %s was excluded by filters set at the annotation processor level. "
                        + "No factory will be generated by toothpick.",
                    typeElement.qualifiedName
                )
                return true
            }
        }
        return false
    }

    // overrides are simpler in this case as methods can only be package or protected.
    // a method with the same name in the type hierarchy would necessarily mean that
    // the {@code methodElement} would be an override of this method.
    protected fun isOverride(typeElement: TypeElement, methodElement: ExecutableElement): Boolean {
        var currentTypeElement: TypeElement? = typeElement
        do {
            if (currentTypeElement !== typeElement) {
                val enclosedElements = currentTypeElement!!.enclosedElements
                for (enclosedElement in enclosedElements) {
                    if (enclosedElement.simpleName == methodElement.simpleName
                        && enclosedElement.getAnnotation(Inject::class.java) != null
                        && enclosedElement.kind == ElementKind.METHOD
                    ) {
                        return true
                    }
                }
            }
            val superclass = currentTypeElement.superclass
            currentTypeElement = if (superclass.kind == TypeKind.DECLARED) {
                val superType = superclass as DeclaredType
                superType.asElement() as TypeElement
            } else {
                null
            }
        } while (currentTypeElement != null)
        return false
    }

    protected fun getMostDirectSuperClassWithInjectedMembers(
        typeElement: TypeElement,
        onlyParents: Boolean
    ): TypeElement? {
        var currentTypeElement: TypeElement? = typeElement
        do {
            if (currentTypeElement !== typeElement || !onlyParents) {
                val enclosedElements = currentTypeElement!!.enclosedElements
                for (enclosedElement in enclosedElements) {
                    if ((enclosedElement.getAnnotation(Inject::class.java) != null
                            && enclosedElement.kind == ElementKind.FIELD)
                        || (enclosedElement.getAnnotation(Inject::class.java) != null
                            && enclosedElement.kind == ElementKind.METHOD)
                    ) {
                        return currentTypeElement
                    }
                }
            }
            val superclass = currentTypeElement.superclass
            currentTypeElement = if (superclass.kind == TypeKind.DECLARED) {
                val superType = superclass as DeclaredType
                superType.asElement() as TypeElement
            } else {
                null
            }
        } while (currentTypeElement != null)
        return null
    }

    protected fun isNonStaticInnerClass(typeElement: TypeElement): Boolean {
        val outerClassOrPackage = typeElement.enclosingElement
        if (outerClassOrPackage.kind != ElementKind.PACKAGE
            && !typeElement.modifiers.contains(Modifier.STATIC)
        ) {
            error(
                typeElement,
                "Class %s is a non static inner class. @Inject constructors are not allowed in non static inner classes.",
                typeElement.qualifiedName
            )
            return true
        }
        return false
    }

    /**
     * Checks if `element` has a @SuppressWarning("`warningSuppressString`") annotation.
     *
     * @param element the element to check if the warning is suppressed.
     * @param warningSuppressString the value of the SuppressWarning annotation.
     * @return true is the injectable warning is suppressed, false otherwise.
     */
    protected fun hasWarningSuppressed(element: Element, warningSuppressString: String?): Boolean {
        return element.getAnnotation(SuppressWarnings::class.java)
            ?.let { suppressWarnings ->
                suppressWarnings.value.any { value ->
                    value.equals(warningSuppressString, ignoreCase = true)
                }
            } ?: false
    }

    /**
     * Lookup both [javax.inject.Qualifier] and [javax.inject.Named] to provide the name
     * of an injection.
     *
     * @param element the element for which a qualifier is to be found.
     * @return the name of this element or null if it has no qualifier annotations.
     */
    private fun findQualifierName(element: VariableElement): String? {
        if (element.annotationMirrors.isEmpty()) return null

        var name: String? = null
        for (annotationMirror in element.annotationMirrors) {
            val annotationTypeElement = annotationMirror.annotationType.asElement() as TypeElement
            when {
                isSameType(annotationTypeElement, "javax.inject.Named") -> {
                    checkIfAlreadyHasName(element, name)
                    name = getValueOfAnnotation(annotationMirror)
                }
                annotationTypeElement.getAnnotation(Qualifier::class.java) != null -> {
                    checkIfAlreadyHasName(element, name)
                    name = annotationTypeElement.qualifiedName.toString()
                }
            }
        }
        return name
    }

    private fun isSameType(typeElement: TypeElement, typeName: String): Boolean {
        return isSameType(typeElement.asType(), typeName)
    }

    private fun isSameType(typeMirror: TypeMirror, typeName: String): Boolean {
        return typeUtils.isSameType(
            typeUtils.erasure(typeMirror),
            typeUtils.erasure(elementUtils.getTypeElement(typeName).asType())
        )
    }

    private fun checkIfAlreadyHasName(element: VariableElement, name: Any?) {
        if (name != null) {
            error(element, "Only one javax.inject.Qualifier annotation is allowed to name injections.")
        }
    }

    private fun getValueOfAnnotation(annotationMirror: AnnotationMirror): String? {
        return annotationMirror.elementValues
            .toList()
            .firstOrNull { (key, _) -> key.simpleName.contentEquals("value") }
            ?.second
            ?.toString()
            ?.replace("\"".toRegex(), "")
    }

    private fun isProviderOrLazy(element: Element): Boolean {
        val kind = getParamInjectionTargetKind(element)
        return kind === ParamInjectionTarget.Kind.PROVIDER || kind === ParamInjectionTarget.Kind.LAZY
    }

    private fun getParamInjectionTargetKind(variableElement: Element): ParamInjectionTarget.Kind? {
        val elementTypeMirror = variableElement.asType()
        return when {
            isSameType(elementTypeMirror, "javax.inject.Provider") -> ParamInjectionTarget.Kind.PROVIDER
            isSameType(elementTypeMirror, "toothpick.Lazy") -> ParamInjectionTarget.Kind.LAZY
            else -> {
                val typeElement = typeUtils.asElement(variableElement.asType())
                if (typeElement.kind != ElementKind.CLASS
                    && typeElement.kind != ElementKind.INTERFACE
                    && typeElement.kind != ElementKind.ENUM
                ) {
                    var enclosingElement = variableElement.enclosingElement
                    while (enclosingElement !is TypeElement) {
                        enclosingElement = enclosingElement.enclosingElement
                    }
                    error(
                        variableElement,
                        "Field %s#%s is of type %s which is not supported by Toothpick.",
                        enclosingElement.qualifiedName,
                        variableElement.simpleName,
                        typeElement
                    )
                    return null
                }
                ParamInjectionTarget.Kind.INSTANCE
            }
        }
    }

    private fun getKindParameter(element: Element): TypeElement {
        val elementTypeMirror = element.asType()
        val firstParameterTypeMirror = (elementTypeMirror as DeclaredType).typeArguments[0]
        return typeUtils.asElement(typeUtils.erasure(firstParameterTypeMirror)) as TypeElement
    }

    @TestOnly
    internal fun setSupportedAnnotationTypes(vararg typeFQNs: String) {
        val current = optionsOverride ?: ToothpickProcessorOptions()
        optionsOverride = current.copy(
            annotationTypes = current.annotationTypes + typeFQNs
        )
    }

    companion object {

        /**
         * The name of the [javax.inject.Inject] annotation class that triggers `ToothpickProcessor`s.
         */
        const val INJECT_ANNOTATION_CLASS_NAME = "javax.inject.Inject"
        const val SINGLETON_ANNOTATION_CLASS_NAME = "javax.inject.Singleton"
        const val PRODUCES_SINGLETON_ANNOTATION_CLASS_NAME = "toothpick.ProvidesSingleton"
        const val INJECT_CONSTRUCTOR_ANNOTATION_CLASS_NAME = "toothpick.InjectConstructor"

        /**
         * The name of the annotation processor option to exclude classes from the creation of member
         * scopes & factories. Exclude filters are java regex, multiple entries are comma separated.
         */
        const val PARAMETER_EXCLUDES = "toothpick_excludes"

        /**
         * The name of the annotation processor option to let TP know about custom scope annotation
         * classes. This option is needed only in the case where a custom scope annotation is used on a
         * class, and this class doesn't use any annotation processed out of the box by TP (i.e.
         * javax.inject.* annotations). If you use custom scope annotations, it is a good practice to
         * always use this option so that developers can use the new scope annotation in a very free way
         * without having to consider the annotation processing internals.
         */
        const val PARAMETER_ANNOTATION_TYPES = "toothpick_annotations"

        /**
         * The name of the annotation processor option to make the TP annotation processor crash when it
         * can't generate a factory for a class. By default the behavior is not to crash but emit a
         * warning. Passing the value `true` crashes the build instead.
         */
        const val PARAMETER_CRASH_WHEN_NO_FACTORY_CAN_BE_CREATED = "toothpick_crash_when_no_factory_can_be_created"

        /**
         * The name of the annotation processor option to make the TP annotation processor crash when it
         * detects an annotated method but with a non package-private visibility. By default the behavior
         * is not to crash but emit a warning. Passing the value `true` crashes the build instead.
         */
        const val PARAMETER_CRASH_WHEN_INJECTED_METHOD_IS_NOT_PACKAGE =
            "toothpick_crash_when_injected_method_is_not_package"

        /** Allows to suppress warning when an injected method is not package-private visible.  */
        private const val SUPPRESS_WARNING_ANNOTATION_VISIBLE_VALUE = "visible"
    }
}
