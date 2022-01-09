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
package toothpick.compiler.factory

import org.jetbrains.annotations.TestOnly
import toothpick.InjectConstructor
import toothpick.ProvidesReleasable
import toothpick.ProvidesSingleton
import toothpick.Releasable
import toothpick.compiler.common.ToothpickProcessor
import toothpick.compiler.common.ToothpickProcessorOptions
import toothpick.compiler.factory.generators.FactoryGenerator
import toothpick.compiler.factory.targets.ConstructorInjectionTarget
import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy
import javax.annotation.processing.RoundEnvironment
import javax.annotation.processing.SupportedOptions
import javax.inject.Inject
import javax.inject.Scope
import javax.inject.Singleton
import javax.lang.model.element.*
import javax.lang.model.util.ElementFilter

/**
 * This processor's role is to create [Factory]. We create factories in different situations :
 *
 *
 *  * When a class `Foo` has an [javax.inject.Inject] annotated constructor : <br></br>
 * --> we create a Factory to create `Foo` instances.
 *
 *
 * The processor will also try to relax the constraints to generate factories in a few cases. These
 * factories are helpful as they require less work from developers :
 *
 *
 *  * When a class `Foo` is annotated with [javax.inject.Singleton] : <br></br>
 * --> it will use the annotated constructor or the default constructor if possible. Otherwise
 * an error is raised.
 *  * When a class `Foo` is annotated with [ProvidesSingleton] : <br></br>
 * --> it will use the annotated constructor or the default constructor if possible. Otherwise
 * an error is raised.
 *  * When a class `Foo` has an [javax.inject.Inject] annotated field `@Inject
 * B b` : <br></br>
 * --> it will use the annotated constructor or the default constructor if possible. Otherwise
 * an error is raised.
 *  * When a class `Foo` has an [javax.inject.Inject] method `@Inject m()` :
 * <br></br>
 * --> it will use the annotated constructor or the default constructor if possible. Otherwise
 * an error is raised.
 *
 *
 * Note that if a class is abstract, the relax mechanism doesn't generate a factory and raises no
 * error.
 */
// http://stackoverflow.com/a/2067863/693752
@SupportedOptions(
    ToothpickProcessor.PARAMETER_EXCLUDES,
    ToothpickProcessor.PARAMETER_ANNOTATION_TYPES,
    ToothpickProcessor.PARAMETER_CRASH_WHEN_NO_FACTORY_CAN_BE_CREATED
)
class FactoryProcessor : ToothpickProcessor() {

    private val allRoundsGeneratedToTypeElement = mutableMapOf<String, TypeElement>()

    override fun getSupportedAnnotationTypes(): Set<String> = options.annotationTypes

    override fun process(annotations: Set<TypeElement>, roundEnv: RoundEnvironment): Boolean {
        val mapTypeElementToConstructorInjectionTarget = findAndParseTargets(roundEnv, annotations)

        // Generate Factories
        for ((typeElement, constructorInjectionTarget) in mapTypeElementToConstructorInjectionTarget) {
            val factoryGenerator = FactoryGenerator(constructorInjectionTarget, typeUtils)
            writeToFile(
                codeGenerator = factoryGenerator,
                fileDescription = "Factory for type %s".format(typeElement),
                originatingElement = typeElement
            )
            allRoundsGeneratedToTypeElement[factoryGenerator.fqcn] = typeElement
        }
        return false
    }

    private fun findAndParseTargets(
        roundEnv: RoundEnvironment,
        annotations: Set<TypeElement>
    ): Map<TypeElement, ConstructorInjectionTarget> {
        val map = mutableMapOf<TypeElement, ConstructorInjectionTarget>()
        createFactoriesForClassesAnnotatedWithInjectConstructor(roundEnv, map)
        createFactoriesForClassesWithInjectAnnotatedConstructors(roundEnv, map)
        createFactoriesForClassesAnnotatedWith(roundEnv, ProvidesSingleton::class.java, map)
        createFactoriesForClassesWithInjectAnnotatedFields(roundEnv, map)
        createFactoriesForClassesWithInjectAnnotatedMethods(roundEnv, map)
        createFactoriesForClassesAnnotatedWithScopeAnnotations(roundEnv, annotations, map)
        return map
    }

    private fun createFactoriesForClassesAnnotatedWithScopeAnnotations(
        roundEnv: RoundEnvironment,
        annotations: Set<TypeElement>,
        mapTypeElementToConstructorInjectionTarget: MutableMap<TypeElement, ConstructorInjectionTarget>
    ) {
        for (annotation in annotations) {
            if (annotation.getAnnotation(Scope::class.java) != null) {
                checkScopeAnnotationValidity(annotation)
                createFactoriesForClassesAnnotatedWith(roundEnv, annotation, mapTypeElementToConstructorInjectionTarget)
            }
        }
    }

    private fun createFactoriesForClassesWithInjectAnnotatedMethods(
        roundEnv: RoundEnvironment,
        mapTypeElementToConstructorInjectionTarget: MutableMap<TypeElement, ConstructorInjectionTarget>
    ) {
        for (methodElement in ElementFilter.methodsIn(roundEnv.getElementsAnnotatedWith(Inject::class.java))) {
            processClassContainingInjectAnnotatedMember(
                enclosingElement = methodElement.enclosingElement,
                mapTypeElementToConstructorInjectionTarget = mapTypeElementToConstructorInjectionTarget
            )
        }
    }

    private fun createFactoriesForClassesWithInjectAnnotatedFields(
        roundEnv: RoundEnvironment,
        mapTypeElementToConstructorInjectionTarget: MutableMap<TypeElement, ConstructorInjectionTarget>
    ) {
        for (fieldElement in ElementFilter.fieldsIn(roundEnv.getElementsAnnotatedWith(Inject::class.java))) {
            processClassContainingInjectAnnotatedMember(
                enclosingElement = fieldElement.enclosingElement,
                mapTypeElementToConstructorInjectionTarget = mapTypeElementToConstructorInjectionTarget
            )
        }
    }

    private fun createFactoriesForClassesAnnotatedWith(
        roundEnv: RoundEnvironment,
        annotationClass: Class<out Annotation?>,
        mapTypeElementToConstructorInjectionTarget: MutableMap<TypeElement, ConstructorInjectionTarget>
    ) {
        for (annotatedElement in ElementFilter.typesIn(roundEnv.getElementsAnnotatedWith(annotationClass))) {
            processClassContainingInjectAnnotatedMember(
                enclosingElement = annotatedElement as TypeElement,
                mapTypeElementToConstructorInjectionTarget = mapTypeElementToConstructorInjectionTarget
            )
        }
    }

    private fun createFactoriesForClassesAnnotatedWith(
        roundEnv: RoundEnvironment,
        annotationType: TypeElement,
        mapTypeElementToConstructorInjectionTarget: MutableMap<TypeElement, ConstructorInjectionTarget>
    ) {
        for (annotatedElement in ElementFilter.typesIn(roundEnv.getElementsAnnotatedWith(annotationType))) {
            val annotatedTypeElement = annotatedElement as TypeElement
            processClassContainingInjectAnnotatedMember(
                enclosingElement = annotatedTypeElement,
                mapTypeElementToConstructorInjectionTarget = mapTypeElementToConstructorInjectionTarget
            )
        }
    }

    private fun createFactoriesForClassesWithInjectAnnotatedConstructors(
        roundEnv: RoundEnvironment,
        mapTypeElementToConstructorInjectionTarget: MutableMap<TypeElement, ConstructorInjectionTarget>
    ) {
        for (constructorElement in ElementFilter.constructorsIn(roundEnv.getElementsAnnotatedWith(Inject::class.java))) {
            val enclosingElement = constructorElement.enclosingElement as TypeElement
            if (!isSingleInjectAnnotatedConstructor(constructorElement)) {
                error(
                    constructorElement,
                    "Class %s cannot have more than one @Inject annotated constructor.",
                    enclosingElement.qualifiedName
                )
            }
            processInjectAnnotatedConstructor(
                constructorElement = constructorElement,
                targetClassMap = mapTypeElementToConstructorInjectionTarget
            )
        }
    }

    private fun createFactoriesForClassesAnnotatedWithInjectConstructor(
        roundEnv: RoundEnvironment,
        mapTypeElementToConstructorInjectionTarget: MutableMap<TypeElement, ConstructorInjectionTarget>
    ) {
        for (annotatedElement in ElementFilter.typesIn(roundEnv.getElementsAnnotatedWith(InjectConstructor::class.java))) {
            val annotatedTypeElement = annotatedElement as TypeElement
            val constructorElements = ElementFilter.constructorsIn(annotatedTypeElement.enclosedElements)
            if (constructorElements.size != 1
                || constructorElements[0].getAnnotation(Inject::class.java) != null
            ) {
                error(
                    constructorElements[0],
                    "Class %s is annotated with @InjectInjectConstructor. Therefore, It must have one unique constructor and it should not be annotated with @Inject.",
                    annotatedTypeElement.qualifiedName
                )
            }
            processInjectAnnotatedConstructor(
                constructorElement = constructorElements[0],
                targetClassMap = mapTypeElementToConstructorInjectionTarget
            )
        }
    }

    private fun processClassContainingInjectAnnotatedMember(
        enclosingElement: Element,
        mapTypeElementToConstructorInjectionTarget: MutableMap<TypeElement, ConstructorInjectionTarget>
    ) {
        val typeElement = typeUtils.asElement(enclosingElement.asType()) as TypeElement
        if (mapTypeElementToConstructorInjectionTarget.containsKey(typeElement)) {
            // the class is already known
            return
        }
        if (isExcludedByFilters(typeElement)) {
            return
        }

        // Verify common generated code restrictions.
        if (!canTypeHaveAFactory(typeElement)) {
            return
        }
        val constructorInjectionTarget = createConstructorInjectionTarget(typeElement)
        if (constructorInjectionTarget != null) {
            mapTypeElementToConstructorInjectionTarget[typeElement] = constructorInjectionTarget
        }
    }

    private fun isSingleInjectAnnotatedConstructor(constructorElement: Element): Boolean {
        val enclosingElement = constructorElement.enclosingElement as TypeElement
        var isSingleInjectedConstructor = true
        val constructorElements = ElementFilter.constructorsIn(enclosingElement.enclosedElements)
        for (constructorElementInClass in constructorElements) {
            if (constructorElementInClass.getAnnotation(Inject::class.java) != null
                && constructorElement != constructorElementInClass
            ) {
                isSingleInjectedConstructor = false
            }
        }
        return isSingleInjectedConstructor
    }

    private fun processInjectAnnotatedConstructor(
        constructorElement: ExecutableElement,
        targetClassMap: MutableMap<TypeElement, ConstructorInjectionTarget>
    ) {
        val enclosingElement = constructorElement.enclosingElement as TypeElement

        // Verify common generated code restrictions.
        if (!isValidInjectAnnotatedConstructor(constructorElement)) {
            return
        }
        if (isExcludedByFilters(enclosingElement)) {
            return
        }
        if (!canTypeHaveAFactory(enclosingElement)) {
            error(
                enclosingElement,
                "The class %s is abstract or private. It cannot have an injected constructor.",
                enclosingElement.qualifiedName
            )
            return
        }
        targetClassMap[enclosingElement] = createConstructorInjectionTarget(constructorElement)
    }

    private fun isValidInjectAnnotatedConstructor(element: ExecutableElement): Boolean {
        val enclosingElement = element.enclosingElement as TypeElement

        // Verify modifiers.
        val modifiers = element.modifiers
        if (modifiers.contains(Modifier.PRIVATE)) {
            error(
                element,
                "@Inject constructors must not be private in class %s.",
                enclosingElement.qualifiedName
            )
            return false
        }

        // Verify parentScope modifiers.
        val parentModifiers = enclosingElement.modifiers
        if (parentModifiers.contains(Modifier.PRIVATE)) {
            error(
                element,
                "Class %s is private. @Inject constructors are not allowed in private classes.",
                enclosingElement.qualifiedName
            )
            return false
        }
        if (isNonStaticInnerClass(enclosingElement)) {
            return false
        }
        for (paramElement in element.parameters) {
            if (!isValidInjectedType(paramElement)) {
                return false
            }
        }
        return true
    }

    private fun createConstructorInjectionTarget(
        constructorElement: ExecutableElement
    ): ConstructorInjectionTarget {
        val enclosingElement = constructorElement.enclosingElement as TypeElement
        val scopeName = getScopeName(enclosingElement)
        val hasSingletonAnnotation = hasSingletonAnnotation(enclosingElement)
        val hasReleasableAnnotation = hasReleasableAnnotation(enclosingElement)
        val hasProvidesSingletonInScopeAnnotation = hasProvidesSingletonInScopeAnnotation(enclosingElement)
        val hasProvidesReleasableAnnotation = hasProvidesReleasableAnnotation(enclosingElement)
        checkReleasableAnnotationValidity(
            enclosingElement, hasReleasableAnnotation, hasSingletonAnnotation
        )
        checkProvidesReleasableAnnotationValidity(
            enclosingElement, hasReleasableAnnotation, hasSingletonAnnotation
        )
        if (hasProvidesSingletonInScopeAnnotation && scopeName == null) {
            error(
                enclosingElement,
                "The type %s uses @ProvidesSingleton but doesn't have a scope annotation.",
                enclosingElement.qualifiedName.toString()
            )
        }
        val superClassWithInjectedMembers = getMostDirectSuperClassWithInjectedMembers(enclosingElement, false)
        val constructorInjectionTarget = ConstructorInjectionTarget(
            enclosingElement,
            scopeName,
            hasSingletonAnnotation,
            hasReleasableAnnotation,
            hasProvidesSingletonInScopeAnnotation,
            hasProvidesReleasableAnnotation,
            superClassWithInjectedMembers
        )
        constructorInjectionTarget.parameters.addAll(getParamInjectionTargetList(constructorElement))
        constructorInjectionTarget.throwsThrowable = !constructorElement.thrownTypes.isEmpty()
        return constructorInjectionTarget
    }

    private fun createConstructorInjectionTarget(typeElement: TypeElement): ConstructorInjectionTarget? {
        val scopeName = getScopeName(typeElement)
        val hasSingletonAnnotation = hasSingletonAnnotation(typeElement)
        val hasReleasableAnnotation = hasReleasableAnnotation(typeElement)
        val hasProvidesSingletonInScopeAnnotation = hasProvidesSingletonInScopeAnnotation(typeElement)
        val hasProvidesReleasableAnnotation = hasProvidesReleasableAnnotation(typeElement)
        checkReleasableAnnotationValidity(typeElement, hasReleasableAnnotation, hasSingletonAnnotation)
        checkProvidesReleasableAnnotationValidity(
            typeElement, hasReleasableAnnotation, hasSingletonAnnotation
        )
        if (hasProvidesSingletonInScopeAnnotation && scopeName == null) {
            error(
                typeElement,
                "The type %s uses @ProvidesSingleton but doesn't have a scope annotation.",
                typeElement.qualifiedName.toString()
            )
        }
        val superClassWithInjectedMembers = getMostDirectSuperClassWithInjectedMembers(typeElement, false)
        val constructorElements = ElementFilter.constructorsIn(typeElement.enclosedElements)
        // we just need to deal with the case of the default constructor only.
        // like Guice, we will call it by default in the optimistic factory
        // injected constructors will be handled at some point in the compilation cycle

        // if there is an injected constructor, it will be caught later, just leave
        for (constructorElement in constructorElements) {
            if (constructorElement.getAnnotation(Inject::class.java) != null) {
                return null
            }
        }
        val cannotCreateAFactoryMessage = (" Toothpick can't create a factory for it."
            + " If this class is itself a DI entry point (i.e. you call TP.inject(this) at some point), "
            + " then you can remove this warning by adding @SuppressWarnings(\"Injectable\") to the class."
            + " A typical example is a class using injection to assign its fields, that calls TP.inject(this),"
            + " but it needs a parameter for its constructor and this parameter is not injectable.")

        // search for default constructor
        for (constructorElement in constructorElements) {
            if (constructorElement.parameters.isEmpty()) {
                if (constructorElement.modifiers.contains(Modifier.PRIVATE)) {
                    if (!isInjectableWarningSuppressed(typeElement)) {
                        val message = String.format(
                            "The class %s has a private default constructor. "
                                + cannotCreateAFactoryMessage,
                            typeElement.qualifiedName.toString()
                        )
                        crashOrWarnWhenNoFactoryCanBeCreated(constructorElement, message)
                    }
                    return null
                }
                return ConstructorInjectionTarget(
                    typeElement,
                    scopeName,
                    hasSingletonAnnotation,
                    hasReleasableAnnotation,
                    hasProvidesSingletonInScopeAnnotation,
                    hasProvidesReleasableAnnotation,
                    superClassWithInjectedMembers
                )
            }
        }
        if (!isInjectableWarningSuppressed(typeElement)) {
            val message =
                "The class ${typeElement.qualifiedName} has injected members or a scope annotation but has no " +
                    "@Inject annotated (non-private) constructor  nor a non-private default constructor. " +
                    cannotCreateAFactoryMessage

            crashOrWarnWhenNoFactoryCanBeCreated(typeElement, message)
        }
        return null
    }

    private fun crashOrWarnWhenNoFactoryCanBeCreated(element: Element, message: String) {
        if (options.crashWhenNoFactoryCanBeCreated) {
            error(element, message)
        } else {
            warning(element, message)
        }
    }

    /**
     * Lookup [javax.inject.Scope] annotated annotations to provide the name of the scope the
     * `typeElement` belongs to. The method logs an error if the `typeElement` has
     * multiple scope annotations.
     *
     * @param typeElement the element for which a scope is to be found.
     * @return the scope of this `typeElement` or `null` if it has no scope annotations.
     */
    private fun getScopeName(typeElement: TypeElement): String? {
        var scopeName: String? = null
        var hasScopeAnnotation = false
        for (annotationMirror in typeElement.annotationMirrors) {
            val annotationTypeElement = annotationMirror.annotationType.asElement() as TypeElement
            val isSingletonAnnotation =
                annotationTypeElement.qualifiedName.contentEquals(SINGLETON_ANNOTATION_CLASS_NAME)
            if (!isSingletonAnnotation && annotationTypeElement.getAnnotation(Scope::class.java) != null) {
                checkScopeAnnotationValidity(annotationTypeElement)
                if (scopeName != null) {
                    error(typeElement, "Only one @Scope qualified annotation is allowed : %s", scopeName)
                }
                scopeName = annotationTypeElement.qualifiedName.toString()
            }
            if (isSingletonAnnotation) {
                hasScopeAnnotation = true
            }
        }
        if (hasScopeAnnotation && scopeName == null) {
            scopeName = SINGLETON_ANNOTATION_CLASS_NAME
        }
        return scopeName
    }

    private fun hasSingletonAnnotation(typeElement: TypeElement): Boolean {
        return typeElement.getAnnotation(Singleton::class.java) != null
    }

    private fun hasReleasableAnnotation(typeElement: TypeElement): Boolean {
        return typeElement.getAnnotation(Releasable::class.java) != null
    }

    private fun hasProvidesSingletonInScopeAnnotation(typeElement: TypeElement): Boolean {
        return typeElement.getAnnotation(ProvidesSingleton::class.java) != null
    }

    private fun hasProvidesReleasableAnnotation(typeElement: TypeElement): Boolean {
        return typeElement.getAnnotation(ProvidesReleasable::class.java) != null
    }

    private fun checkReleasableAnnotationValidity(
        typeElement: TypeElement, hasReleasableAnnotation: Boolean, hasSingletonAnnotation: Boolean
    ) {
        if (hasReleasableAnnotation && !hasSingletonAnnotation) {
            error(
                typeElement, "Class %s is annotated with @Releasable, "
                    + "it should also be annotated with either @Singleton.",
                typeElement.qualifiedName
            )
        }
    }

    private fun checkProvidesReleasableAnnotationValidity(
        typeElement: TypeElement,
        hasProvidesReleasableAnnotation: Boolean,
        hasProvideSingletonInScopeAnnotation: Boolean
    ) {
        if (hasProvidesReleasableAnnotation && !hasProvideSingletonInScopeAnnotation) {
            error(
                typeElement, "Class %s is annotated with @ProvidesReleasable, "
                    + "it should also be annotated with either @ProvidesSingleton.",
                typeElement.qualifiedName
            )
        }
    }

    private fun checkScopeAnnotationValidity(annotation: TypeElement) {
        if (annotation.getAnnotation(Scope::class.java) == null) {
            error(
                annotation,
                "Scope Annotation %s does not contain Scope annotation.",
                annotation.qualifiedName
            )
            return
        }
        val retention = annotation.getAnnotation(Retention::class.java)
        if (retention == null || retention.value != RetentionPolicy.RUNTIME) {
            error(
                annotation,
                "Scope Annotation %s does not have RUNTIME retention policy.",
                annotation.qualifiedName
            )
        }
    }

    /**
     * Checks if the injectable warning is suppressed for the TypeElement, through the usage
     * of @SuppressWarning("Injectable").
     *
     * @param typeElement the element to check if the warning is suppressed.
     * @return true is the injectable warning is suppressed, false otherwise.
     */
    private fun isInjectableWarningSuppressed(typeElement: TypeElement): Boolean {
        return hasWarningSuppressed(typeElement, SUPPRESS_WARNING_ANNOTATION_INJECTABLE_VALUE)
    }

    private fun canTypeHaveAFactory(typeElement: TypeElement): Boolean {
        val isAbstract = typeElement.modifiers.contains(Modifier.ABSTRACT)
        val isPrivate = typeElement.modifiers.contains(Modifier.PRIVATE)
        return !isAbstract && !isPrivate
    }

    @TestOnly
    internal fun setCrashWhenNoFactoryCanBeCreated(crashWhenNoFactoryCanBeCreated: Boolean) {
        val current = optionsOverride ?: ToothpickProcessorOptions()
        optionsOverride = current.copy(
            crashWhenNoFactoryCanBeCreated = crashWhenNoFactoryCanBeCreated
        )
    }

    @TestOnly
    internal fun getOriginatingElement(generatedQualifiedName: String): TypeElement? {
        return allRoundsGeneratedToTypeElement[generatedQualifiedName]
    }

    companion object {
        private const val SUPPRESS_WARNING_ANNOTATION_INJECTABLE_VALUE = "injectable"
    }
}