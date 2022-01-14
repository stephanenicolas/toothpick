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
import toothpick.compiler.common.generators.asElement
import toothpick.compiler.common.generators.erased
import toothpick.compiler.common.generators.hasAnnotation
import toothpick.compiler.common.generators.targets.ParamInjectionTarget
import toothpick.compiler.memberinjector.targets.FieldInjectionTarget
import java.io.IOException
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

    protected fun writeToFile(codeGenerator: CodeGenerator, fileDescription: String): Boolean {
        return try {
            codeGenerator.brewCode().writeTo(filer)
            true
        } catch (e: IOException) {
            error("Error writing %s file: %s", fileDescription, e.message)
            false
        }
    }

    protected fun VariableElement.isValidInjectAnnotatedFieldOrParameter(): Boolean {
        val enclosingElement = enclosingElement as TypeElement

        // Verify modifiers.
        val modifiers = modifiers
        if (modifiers.contains(Modifier.PRIVATE)) {
            error(
                this,
                "@Inject annotated fields must be non private : %s#%s",
                enclosingElement.qualifiedName,
                simpleName
            )
            return false
        }

        // Verify parentScope modifiers.
        if (enclosingElement.modifiers.contains(Modifier.PRIVATE)) {
            error(
                this,
                "@Injected fields in class %s. The class must be non private.",
                enclosingElement.simpleName
            )
            return false
        }

        return isValidInjectedType()
    }

    protected fun ExecutableElement.isValidInjectAnnotatedMethod(): Boolean {
        val enclosingElement = enclosingElement as TypeElement

        // Verify modifiers.
        if (modifiers.contains(Modifier.PRIVATE)) {
            error(
                this,
                "@Inject annotated methods must not be private : %s#%s",
                enclosingElement.qualifiedName,
                simpleName
            )
            return false
        }

        // Verify parentScope modifiers.
        if (enclosingElement.modifiers.contains(Modifier.PRIVATE)) {
            error(
                this,
                "@Injected fields in class %s. The class must be non private.",
                enclosingElement.simpleName
            )
            return false
        }

        if (parameters.any { paramElement -> !paramElement.isValidInjectedType() }) {
            return false
        }

        if (modifiers.contains(Modifier.PUBLIC) || modifiers.contains(Modifier.PROTECTED)) {
            if (!hasWarningSuppressed(SUPPRESS_WARNING_ANNOTATION_VISIBLE_VALUE)) {
                crashOrWarnWhenMethodIsNotPackageVisible(
                    this,
                    "@Inject annotated methods should have package visibility: ${enclosingElement.qualifiedName}#$simpleName"
                )
            }
        }
        return true
    }

    protected fun VariableElement.isValidInjectedType(): Boolean {
        return if (!isValidInjectedElementKind()) false
        else !isProviderOrLazy() || isValidProviderOrLazy()
    }

    private fun VariableElement.isValidInjectedElementKind(): Boolean {
        val typeElement: Element? = asType().asElement(typeUtils)

        // typeElement can be null for primitives.
        // https://github.com/stephanenicolas/toothpick/issues/261
        if (typeElement == null
            || typeElement.kind != ElementKind.CLASS
            && typeElement.kind != ElementKind.INTERFACE
            && typeElement.kind != ElementKind.ENUM
        ) {
            // find the class containing the element
            // the element can be a field or a parameter
            var enclosingElement = enclosingElement
            val typeName = typeElement?.toString() ?: asType().toString()

            when (enclosingElement) {
                is TypeElement -> {
                    error(
                        this,
                        "Field %s#%s is of type %s which is not supported by Toothpick.",
                        enclosingElement.qualifiedName,
                        simpleName,
                        typeName
                    )
                }
                else -> {
                    val methodOrConstructorElement = enclosingElement
                    enclosingElement = enclosingElement.enclosingElement
                    error(
                        this,
                        "Parameter %s in method/constructor %s#%s is of type %s which is not supported by Toothpick.",
                        simpleName,
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

    private fun Element.isValidProviderOrLazy(): Boolean {
        val declaredType = asType() as DeclaredType

        // Contains type parameter
        if (declaredType.typeArguments.isEmpty()) {
            val enclosingElement = enclosingElement
            if (enclosingElement is TypeElement) {
                error(
                    this,
                    "Field %s#%s is not a valid %s.",
                    enclosingElement.qualifiedName,
                    simpleName,
                    declaredType
                )
            } else {
                error(
                    this,
                    "Parameter %s in method/constructor %s#%s is not a valid %s.",
                    simpleName,
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
                val enclosingElement = enclosingElement
                error(
                    this,
                    "Lazy/Provider %s is not a valid in %s. Lazy/Provider cannot be used on generic types.",
                    simpleName,
                    enclosingElement.simpleName
                )
                return false
            }
        }
        return true
    }

    protected fun ExecutableElement.getParamInjectionTargetList(): List<ParamInjectionTarget> =
        parameters.map { variableElement -> variableElement.createFieldOrParamInjectionTarget() }

    protected fun ExecutableElement.getExceptionTypes(): List<TypeElement> =
        thrownTypes.map { type -> (type as DeclaredType).asElement() as TypeElement }

    protected fun VariableElement.createFieldOrParamInjectionTarget(): FieldInjectionTarget {
        return FieldInjectionTarget(
            memberClass = asType().asElement(typeUtils) as TypeElement,
            memberName = simpleName.toString(),
            kind = getParamInjectionTargetKind(),
            kindParamClass = getInjectedType(),
            name = findQualifierName()
        )
    }

    /**
     * Retrieves the type of a field or param. The type can be the type of the parameter in the java
     * way (e.g. `B b`, type is `B`); but it can also be the type of a [ ] or [javax.inject.Provider] (e.g. `Lazy&lt;B&gt; b`, type is `B` not `Lazy`).
     *
     * @param variableElement the field or variable element. NOT his type !
     * @return the type has defined above.
     */
    private fun VariableElement.getInjectedType(): TypeElement {
        return when (getParamInjectionTargetKind()) {
            ParamInjectionTarget.Kind.INSTANCE ->
                asType()
                    .erased(typeUtils)
                    .asElement(typeUtils)
                    as TypeElement
            else -> getKindParameter()
        }
    }

    protected fun TypeElement.isExcludedByFilters(): Boolean {
        val typeElementName = qualifiedName.toString()
        return options.excludes
            .map { exclude -> exclude.toRegex() }
            .any { exclude -> typeElementName.matches(exclude) }
            .also { isExcluded ->
                if (isExcluded) {
                    warning(
                        this,
                        "The class %s was excluded by filters set at the annotation processor level. "
                            + "No factory will be generated by toothpick.",
                        qualifiedName
                    )
                }
            }
    }

    // overrides are simpler in this case as methods can only be package or protected.
    // a method with the same name in the type hierarchy would necessarily mean that
    // the {@code methodElement} would be an override of this method.
    protected fun TypeElement.isOverride(methodElement: ExecutableElement): Boolean {
        var currentTypeElement: TypeElement? = this
        do {
            if (currentTypeElement !== this) {
                val enclosedElements = currentTypeElement!!.enclosedElements
                for (enclosedElement in enclosedElements) {
                    if (enclosedElement.simpleName == methodElement.simpleName
                        && enclosedElement.hasAnnotation<Inject>()
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

    protected fun TypeElement.getMostDirectSuperClassWithInjectedMembers(onlyParents: Boolean): TypeElement? {
        var currentTypeElement: TypeElement? = this
        do {
            if (currentTypeElement !== this || !onlyParents) {
                val enclosedElements = currentTypeElement!!.enclosedElements
                for (enclosedElement in enclosedElements) {
                    if ((enclosedElement.hasAnnotation<Inject>()
                            && enclosedElement.kind == ElementKind.FIELD)
                        || (enclosedElement.hasAnnotation<Inject>()
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
    protected fun Element.hasWarningSuppressed(warningSuppressString: String?): Boolean {
        return getAnnotation(SuppressWarnings::class.java)
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
    private fun VariableElement.findQualifierName(): String? {
        if (annotationMirrors.isEmpty()) return null

        var name: String? = null
        for (annotationMirror in annotationMirrors) {
            val annotationTypeElement = annotationMirror.annotationType.asElement() as TypeElement
            when {
                annotationTypeElement.isSameType("javax.inject.Named") -> {
                    checkIfAlreadyHasName(name)
                    name = annotationMirror.getValueOfAnnotation()
                }
                annotationTypeElement.hasAnnotation<Qualifier>() -> {
                    checkIfAlreadyHasName(name)
                    name = annotationTypeElement.qualifiedName.toString()
                }
            }
        }
        return name
    }

    private fun TypeElement.isSameType(typeName: String): Boolean {
        return asType().isSameType(typeName)
    }

    private fun TypeMirror.isSameType(typeName: String): Boolean {
        return typeUtils.isSameType(
            typeUtils.erasure(this),
            typeUtils.erasure(elementUtils.getTypeElement(typeName).asType())
        )
    }

    private fun VariableElement.checkIfAlreadyHasName(name: Any?) {
        if (name != null) {
            error(this, "Only one javax.inject.Qualifier annotation is allowed to name injections.")
        }
    }

    private fun AnnotationMirror.getValueOfAnnotation(): String? {
        return elementValues
            .toList()
            .firstOrNull { (key, _) -> key.simpleName.contentEquals("value") }
            ?.second
            ?.toString()
            ?.replace("\"", "")
    }

    private fun Element.isProviderOrLazy(): Boolean {
        val kind = getParamInjectionTargetKind()
        return kind === ParamInjectionTarget.Kind.PROVIDER || kind === ParamInjectionTarget.Kind.LAZY
    }

    private fun Element.getParamInjectionTargetKind(): ParamInjectionTarget.Kind? {
        val elementTypeMirror = this.asType()
        return when {
            elementTypeMirror.isSameType("javax.inject.Provider") -> ParamInjectionTarget.Kind.PROVIDER
            elementTypeMirror.isSameType("toothpick.Lazy") -> ParamInjectionTarget.Kind.LAZY
            else -> {
                val typeElement = elementTypeMirror.asElement(typeUtils)!!
                if (typeElement.kind != ElementKind.CLASS
                    && typeElement.kind != ElementKind.INTERFACE
                    && typeElement.kind != ElementKind.ENUM
                ) {
                    var enclosingElement = this.enclosingElement
                    while (enclosingElement !is TypeElement) {
                        enclosingElement = enclosingElement.enclosingElement
                    }
                    error(
                        this,
                        "Field %s#%s is of type %s which is not supported by Toothpick.",
                        enclosingElement.qualifiedName,
                        simpleName,
                        typeElement
                    )
                    return null
                }
                ParamInjectionTarget.Kind.INSTANCE
            }
        }
    }

    private fun Element.getKindParameter(): TypeElement {
        val elementTypeMirror = asType()
        val firstParameterTypeMirror = (elementTypeMirror as DeclaredType).typeArguments[0]
        return firstParameterTypeMirror.erased(typeUtils).asElement(typeUtils) as TypeElement
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
