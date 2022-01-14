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
import toothpick.*
import toothpick.compiler.common.ToothpickProcessor
import toothpick.compiler.common.ToothpickProcessorOptions
import toothpick.compiler.common.generators.*
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

    override fun getSupportedAnnotationTypes(): Set<String> = options.annotationTypes

    override fun process(annotations: Set<TypeElement>, roundEnv: RoundEnvironment): Boolean {
        val mapTypeElementToConstructorInjectionTarget: Map<TypeElement, ConstructorInjectionTarget> =
            with(roundEnv) {
                createFactoriesForClassesAnnotatedWithInjectConstructor() +
                    createFactoriesForClassesWithInjectAnnotatedConstructors() +
                    createFactoriesForClassesAnnotatedWith(ProvidesSingleton::class.java) +
                    createFactoriesForClassesWithInjectAnnotatedFields() +
                    createFactoriesForClassesWithInjectAnnotatedMethods() +
                    createFactoriesForClassesAnnotatedWithScopeAnnotations(annotations)
            }

        // Generate Factories
        mapTypeElementToConstructorInjectionTarget
            .mapValues { (_, target) -> FactoryGenerator(target, typeUtils) }
            .forEach { (typeElement, factoryGenerator) ->
                writeToFile(
                    codeGenerator = factoryGenerator,
                    fileDescription = "Factory for type $typeElement"
                )

                allRoundsGeneratedToTypeElement[factoryGenerator.fqcn] = typeElement
            }

        return false
    }

    private fun RoundEnvironment.createFactoriesForClassesAnnotatedWithScopeAnnotations(annotations: Set<TypeElement>): List<Pair<TypeElement, ConstructorInjectionTarget>> {
        return annotations
            .filter { annotation -> annotation.hasAnnotation<Scope>() }
            .flatMap { annotation ->
                checkScopeAnnotationValidity(annotation)
                createFactoriesForClassesAnnotatedWith(annotation)
            }
    }

    private fun RoundEnvironment.createFactoriesForClassesWithInjectAnnotatedMethods(): List<Pair<TypeElement, ConstructorInjectionTarget>> {
        return getElementsAnnotatedWith(Inject::class.java)
            .methods
            .mapNotNull { methodElement ->
                methodElement.enclosingElement.processClassContainingInjectAnnotatedMember()
            }
    }

    private fun RoundEnvironment.createFactoriesForClassesWithInjectAnnotatedFields(): List<Pair<TypeElement, ConstructorInjectionTarget>> {
        return getElementsAnnotatedWith(Inject::class.java)
            .fields
            .mapNotNull { fieldElement ->
                fieldElement.enclosingElement.processClassContainingInjectAnnotatedMember()
            }
    }

    private fun RoundEnvironment.createFactoriesForClassesAnnotatedWith(annotationClass: Class<out Annotation?>): List<Pair<TypeElement, ConstructorInjectionTarget>> {
        return getElementsAnnotatedWith(annotationClass)
            .types
            .mapNotNull { annotatedElement ->
                annotatedElement.processClassContainingInjectAnnotatedMember()
            }
    }

    private fun RoundEnvironment.createFactoriesForClassesAnnotatedWith(annotationType: TypeElement): List<Pair<TypeElement, ConstructorInjectionTarget>> {
        return getElementsAnnotatedWith(annotationType)
            .types
            .mapNotNull { type ->
                type.processClassContainingInjectAnnotatedMember()
            }
    }

    private fun RoundEnvironment.createFactoriesForClassesWithInjectAnnotatedConstructors(): Map<TypeElement, ConstructorInjectionTarget> {
        return getElementsAnnotatedWith(Inject::class.java)
            .constructors
            .mapNotNull { constructorElement ->
                val enclosingElement = constructorElement.enclosingElement as TypeElement
                if (!constructorElement.isSingleInjectAnnotatedConstructor()) {
                    error(
                        constructorElement,
                        "Class %s cannot have more than one @Inject annotated constructor.",
                        enclosingElement.qualifiedName
                    )
                }

                constructorElement.processInjectAnnotatedConstructor()
            }
            .toMap()
    }

    private fun RoundEnvironment.createFactoriesForClassesAnnotatedWithInjectConstructor(): Map<TypeElement, ConstructorInjectionTarget> {
        return getElementsAnnotatedWith(InjectConstructor::class.java)
            .types
            .mapNotNull { annotatedTypeElement ->
                val constructorElements = annotatedTypeElement.enclosedElements.constructors
                val firstConstructor = constructorElements.firstOrNull()

                if (constructorElements.size == 1
                    && firstConstructor != null
                    && !firstConstructor.hasAnnotation<Inject>()
                ) {
                    firstConstructor.processInjectAnnotatedConstructor()
                } else {
                    error(
                        constructorElements.firstOrNull(),
                        "Class %s is annotated with @InjectConstructor. Therefore, It must have one unique constructor and it should not be annotated with @Inject.",
                        annotatedTypeElement.qualifiedName
                    )
                    null
                }
            }
            .toMap()
    }

    private fun Element.processClassContainingInjectAnnotatedMember(): Pair<TypeElement, ConstructorInjectionTarget>? {
        val typeElement = asType().asElement(typeUtils) as TypeElement
        if (typeElement.isExcludedByFilters()) return null

        // Verify common generated code restrictions.
        if (!typeElement.canTypeHaveAFactory()) return null

        val target = typeElement.createConstructorInjectionTarget()
        return if (target == null) null else typeElement to target
    }

    private fun Element.isSingleInjectAnnotatedConstructor(): Boolean {
        return enclosingElement
            .enclosedElements
            .constructors
            .all { element -> element == this || !element.hasAnnotation<Inject>() }
    }

    private fun ExecutableElement.processInjectAnnotatedConstructor(): Pair<TypeElement, ConstructorInjectionTarget>? {
        val enclosingElement = enclosingElement as TypeElement

        // Verify common generated code restrictions.
        if (!isValidInjectAnnotatedConstructor()) return null
        if (enclosingElement.isExcludedByFilters()) return null
        if (!enclosingElement.canTypeHaveAFactory()) {
            error(
                enclosingElement,
                "The class %s is abstract or private. It cannot have an injected constructor.",
                enclosingElement.qualifiedName
            )
            return null
        }

        return enclosingElement to createConstructorInjectionTarget()
    }

    private fun ExecutableElement.isValidInjectAnnotatedConstructor(): Boolean {
        val enclosingElement = enclosingElement as TypeElement

        // Verify modifiers.
        val modifiers = modifiers
        if (modifiers.contains(Modifier.PRIVATE)) {
            error(
                this,
                "@Inject constructors must not be private in class %s.",
                enclosingElement.qualifiedName
            )
            return false
        }

        // Verify parentScope modifiers.
        val parentModifiers = enclosingElement.modifiers
        if (parentModifiers.contains(Modifier.PRIVATE)) {
            error(
                this,
                "Class %s is private. @Inject constructors are not allowed in private classes.",
                enclosingElement.qualifiedName
            )
            return false
        }

        if (isNonStaticInnerClass(enclosingElement)) return false

        return parameters.all { param -> param.isValidInjectedType() }
    }

    private fun ExecutableElement.createConstructorInjectionTarget(): ConstructorInjectionTarget {
        val enclosingElement = enclosingElement as TypeElement
        val scopeName = enclosingElement.getScopeName()

        enclosingElement.checkReleasableAnnotationValidity()
        enclosingElement.checkProvidesReleasableAnnotationValidity()

        if (enclosingElement.hasAnnotation<ProvidesSingleton>() && scopeName == null) {
            error(
                enclosingElement,
                "The type %s uses @ProvidesSingleton but doesn't have a scope annotation.",
                enclosingElement.qualifiedName.toString()
            )
        }

        val superClassWithInjectedMembers =
            enclosingElement.getMostDirectSuperClassWithInjectedMembers(onlyParents = false)

        return ConstructorInjectionTarget(
            builtClass = enclosingElement,
            scopeName = scopeName,
            hasSingletonAnnotation = enclosingElement.hasAnnotation<Singleton>(),
            hasReleasableAnnotation = enclosingElement.hasAnnotation<Releasable>(),
            hasProvidesSingletonInScopeAnnotation = enclosingElement.hasAnnotation<ProvidesSingletonInScope>(),
            hasProvidesReleasableAnnotation = enclosingElement.hasAnnotation<ProvidesReleasable>(),
            superClassThatNeedsMemberInjection = superClassWithInjectedMembers,
            parameters = getParamInjectionTargetList(),
            throwsThrowable = thrownTypes.isNotEmpty()
        )
    }

    private fun TypeElement.createConstructorInjectionTarget(): ConstructorInjectionTarget? {
        val scopeName = getScopeName()
        checkReleasableAnnotationValidity()
        checkProvidesReleasableAnnotationValidity()

        if (hasAnnotation<ProvidesSingleton>() && scopeName == null) {
            error(
                this,
                "The type %s uses @ProvidesSingleton but doesn't have a scope annotation.",
                qualifiedName.toString()
            )
        }

        val superClassWithInjectedMembers = getMostDirectSuperClassWithInjectedMembers(onlyParents = false)
        val constructorElements = enclosedElements.constructors

        // we just need to deal with the case of the default constructor only.
        // like Guice, we will call it by default in the optimistic factory
        // injected constructors will be handled at some point in the compilation cycle

        // if there is an injected constructor, it will be caught later, just leave
        if (constructorElements.any { element -> element.hasAnnotation<Inject>() }) return null

        val cannotCreateAFactoryMessage = (" Toothpick can't create a factory for it."
            + " If this class is itself a DI entry point (i.e. you call TP.inject(this) at some point), "
            + " then you can remove this warning by adding @SuppressWarnings(\"Injectable\") to the class."
            + " A typical example is a class using injection to assign its fields, that calls TP.inject(this),"
            + " but it needs a parameter for its constructor and this parameter is not injectable.")

        // search for default constructor
        for (constructorElement in constructorElements) {
            if (constructorElement.parameters.isEmpty()) {
                if (constructorElement.modifiers.contains(Modifier.PRIVATE)) {
                    if (!isInjectableWarningSuppressed()) {
                        val message = String.format(
                            "The class %s has a private default constructor. $cannotCreateAFactoryMessage",
                            qualifiedName.toString()
                        )

                        constructorElement.crashOrWarnWhenNoFactoryCanBeCreated(message)
                    }
                    return null
                }

                return ConstructorInjectionTarget(
                    builtClass = this,
                    scopeName = scopeName,
                    hasSingletonAnnotation = hasAnnotation<Singleton>(),
                    hasReleasableAnnotation = hasAnnotation<Releasable>(),
                    hasProvidesSingletonInScopeAnnotation = hasAnnotation<ProvidesSingletonInScope>(),
                    hasProvidesReleasableAnnotation = hasAnnotation<ProvidesReleasable>(),
                    superClassThatNeedsMemberInjection = superClassWithInjectedMembers
                )
            }
        }

        if (!isInjectableWarningSuppressed()) {
            val message =
                "The class $qualifiedName has injected members or a scope annotation but has no " +
                    "@Inject annotated (non-private) constructor  nor a non-private default constructor. " +
                    cannotCreateAFactoryMessage

            crashOrWarnWhenNoFactoryCanBeCreated(message)
        }
        return null
    }

    private fun Element.crashOrWarnWhenNoFactoryCanBeCreated(message: String) {
        if (options.crashWhenNoFactoryCanBeCreated) {
            error(this, message)
        } else {
            warning(this, message)
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
    private fun TypeElement.getScopeName(): String? {
        var scopeName: String? = null
        var hasScopeAnnotation = false

        annotationMirrors
            .map { annotationMirror -> annotationMirror.annotationType.asElement() as TypeElement }
            .forEach { annotationTypeElement ->
                val isSingletonAnnotation =
                    annotationTypeElement.qualifiedName.contentEquals(SINGLETON_ANNOTATION_CLASS_NAME)

                if (!isSingletonAnnotation && annotationTypeElement.hasAnnotation<Scope>()) {
                    checkScopeAnnotationValidity(annotationTypeElement)
                    if (scopeName != null) {
                        error(this, "Only one @Scope qualified annotation is allowed : %s", scopeName)
                    }
                    scopeName = annotationTypeElement.qualifiedName.toString()
                }

                if (isSingletonAnnotation) {
                    hasScopeAnnotation = true
                }
            }

        if (hasScopeAnnotation && scopeName == null) {
            return SINGLETON_ANNOTATION_CLASS_NAME
        }

        return scopeName
    }

    private fun TypeElement.checkReleasableAnnotationValidity() {
        if (hasAnnotation<Releasable>() && !hasAnnotation<Singleton>()) {
            error(
                this,
                "Class %s is annotated with @Releasable, it should also be annotated with @Singleton.",
                qualifiedName
            )
        }
    }

    private fun TypeElement.checkProvidesReleasableAnnotationValidity() {
        if (hasAnnotation<ProvidesReleasable>() && !hasAnnotation<ProvidesSingletonInScope>()) {
            error(
                this, "Class %s is annotated with @ProvidesReleasable, "
                    + "it should also be annotated with either @ProvidesSingleton.",
                qualifiedName
            )
        }
    }

    private fun checkScopeAnnotationValidity(annotation: TypeElement) {
        if (!annotation.hasAnnotation<Scope>()) {
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
    private fun TypeElement.isInjectableWarningSuppressed(): Boolean =
        hasWarningSuppressed(SUPPRESS_WARNING_ANNOTATION_INJECTABLE_VALUE)

    private fun TypeElement.canTypeHaveAFactory(): Boolean {
        val isAbstract = modifiers.contains(Modifier.ABSTRACT)
        val isPrivate = modifiers.contains(Modifier.PRIVATE)
        return !isAbstract && !isPrivate
    }

    @TestOnly
    internal fun setCrashWhenNoFactoryCanBeCreated(crashWhenNoFactoryCanBeCreated: Boolean) {
        val current = optionsOverride ?: ToothpickProcessorOptions()
        optionsOverride = current.copy(
            crashWhenNoFactoryCanBeCreated = crashWhenNoFactoryCanBeCreated
        )
    }

    private val allRoundsGeneratedToTypeElement = mutableMapOf<String, TypeElement>()

    @TestOnly
    internal fun getOriginatingElement(generatedQualifiedName: String): TypeElement? =
        allRoundsGeneratedToTypeElement[generatedQualifiedName]

    companion object {
        private const val SUPPRESS_WARNING_ANNOTATION_INJECTABLE_VALUE = "injectable"
    }
}