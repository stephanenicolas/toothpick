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

import com.google.devtools.ksp.*
import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.symbol.*
import toothpick.*
import toothpick.compiler.common.ToothpickProcessor
import toothpick.compiler.common.generators.*
import toothpick.compiler.factory.generators.FactoryGenerator
import toothpick.compiler.factory.targets.ConstructorInjectionTarget
import java.lang.annotation.RetentionPolicy
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
@OptIn(KspExperimental::class)
class FactoryProcessor(
    processorOptions: Map<String, String>,
    codeGenerator: CodeGenerator,
    logger: KSPLogger
) : ToothpickProcessor(
    processorOptions, codeGenerator, logger
) {
    override fun process(resolver: Resolver): List<KSAnnotated> {
        val mapTypeElementToConstructorInjectionTarget: Map<KSClassDeclaration, ConstructorInjectionTarget> =
            with(resolver) {
                createFactoriesForClassesAnnotatedWithInjectConstructor() +
                    createFactoriesForClassesWithInjectAnnotatedConstructors() +
                    createFactoriesForClassesAnnotatedWith(ProvidesSingleton::class.qualifiedName!!) +
                    createFactoriesForClassesWithInjectAnnotatedFields() +
                    createFactoriesForClassesWithInjectAnnotatedMethods() +
                    createFactoriesForClassesAnnotatedWithScopeAnnotations(supportedAnnotationTypes)
            }.toMap()

        // Generate Factories
        mapTypeElementToConstructorInjectionTarget
            .mapValues { (_, target) -> FactoryGenerator(target) }
            .forEach { (typeElement, factoryGenerator) ->
                writeToFile(
                    tpCodeGenerator = factoryGenerator,
                    fileDescription = "Factory for type $typeElement"
                )

                if (options.debugLogOriginatingElements) {
                    logger.info(
                        "%s generated class %s",
                        factoryGenerator.sourceClassName.toString(),
                        factoryGenerator.generatedClassName.toString()
                    )
                }
            }

        return emptyList()
    }

    private val Resolver.supportedAnnotationTypes: Set<KSClassDeclaration>
        get() = options.supportedAnnotationTypes
            .mapNotNull { className -> getClassDeclarationByName(className) }
            .toSet()

    private fun Resolver.createFactoriesForClassesAnnotatedWithScopeAnnotations(annotations: Set<KSClassDeclaration>): List<Pair<KSClassDeclaration, ConstructorInjectionTarget>> {
        return annotations
            .filter { annotation -> annotation.isAnnotationPresent(Scope::class) }
            .filter { annotation -> annotation.checkScopeAnnotationValidity() }
            .mapNotNull { annotation -> annotation.qualifiedName?.asString() }
            .flatMap { annotationName -> createFactoriesForClassesAnnotatedWith(annotationName) }
    }

    private fun Resolver.createFactoriesForClassesWithInjectAnnotatedMethods(): Sequence<Pair<KSClassDeclaration, ConstructorInjectionTarget>> {
        return getSymbolsWithAnnotation(Inject::class.qualifiedName!!)
            .filterIsInstance<KSFunctionDeclaration>()
            .filter { function -> function.functionKind == FunctionKind.MEMBER }
            .mapNotNull { function -> function.getParentClassOrNull() }
            .distinct()
            .mapNotNull { parentClass -> parentClass.processClassContainingInjectAnnotatedMember() }
    }

    private fun Resolver.createFactoriesForClassesWithInjectAnnotatedFields(): Sequence<Pair<KSClassDeclaration, ConstructorInjectionTarget>> {
        return getSymbolsWithAnnotation(Inject::class.qualifiedName!!)
            .filterIsInstance<KSPropertyDeclaration>()
            .mapNotNull { property -> property.getParentClassOrNull() }
            .distinct()
            .mapNotNull { parentClass -> parentClass.processClassContainingInjectAnnotatedMember() }
    }

    private fun Resolver.createFactoriesForClassesAnnotatedWith(annotationName: String): Sequence<Pair<KSClassDeclaration, ConstructorInjectionTarget>> {
        return getSymbolsWithAnnotation(annotationName)
            .filterIsInstance<KSClassDeclaration>()
            .mapNotNull { element -> element.processClassContainingInjectAnnotatedMember() }
    }

    private fun Resolver.createFactoriesForClassesWithInjectAnnotatedConstructors(): Sequence<Pair<KSClassDeclaration, ConstructorInjectionTarget>> {
        return getSymbolsWithAnnotation(Inject::class.qualifiedName!!)
            .filterIsInstance<KSFunctionDeclaration>()
            .filter { function -> function.functionKind == FunctionKind.MEMBER }
            .filter { function -> function.isConstructor() }
            .mapNotNull { constructor ->
                if (!constructor.isSingleInjectAnnotatedConstructor()) {
                    logger.error(
                        constructor,
                        "Class %s cannot have more than one @Inject-annotated constructor.",
                        constructor.parentDeclaration?.qualifiedName?.asString()
                    )
                }

                constructor.processInjectAnnotatedConstructor()
            }
    }

    private fun Resolver.createFactoriesForClassesAnnotatedWithInjectConstructor(): Sequence<Pair<KSClassDeclaration, ConstructorInjectionTarget>> {
        return getSymbolsWithAnnotation(InjectConstructor::class.qualifiedName!!)
            .filterIsInstance<KSClassDeclaration>()
            .mapNotNull { element ->
                val constructorElements = element.getConstructors()
                val firstConstructor = constructorElements.firstOrNull()

                if (constructorElements.count() == 1
                    && firstConstructor != null
                    && !firstConstructor.isAnnotationPresent(Inject::class)
                ) {
                    firstConstructor.processInjectAnnotatedConstructor()
                } else {
                    logger.error(
                        constructorElements.firstOrNull(),
                        "Class %s is annotated with @InjectConstructor. Therefore, It must have one unique constructor and it should not be annotated with @Inject.",
                        element.qualifiedName?.asString()
                    )
                    null
                }
            }
    }

    private fun KSClassDeclaration.processClassContainingInjectAnnotatedMember(): Pair<KSClassDeclaration, ConstructorInjectionTarget>? {
        if (isExcludedByFilters()) return null

        // Verify common generated code restrictions.
        if (!canTypeHaveAFactory()) return null

        val target = createConstructorInjectionTarget()
        return if (target == null) null else this to target
    }

    private fun KSFunctionDeclaration.isSingleInjectAnnotatedConstructor(): Boolean {
        return (parentDeclaration as KSClassDeclaration)
            .getConstructors()
            .all { constructor ->
                constructor == this || !constructor.isAnnotationPresent(Inject::class)
            }
    }

    private fun KSFunctionDeclaration.processInjectAnnotatedConstructor(): Pair<KSClassDeclaration, ConstructorInjectionTarget>? {
        val parentClass = parentDeclaration as KSClassDeclaration

        // Verify common generated code restrictions.
        if (!isValidInjectAnnotatedConstructor()) return null
        if (parentClass.isExcludedByFilters()) return null
        if (!parentClass.canTypeHaveAFactory()) {
            logger.error(
                parentClass,
                "The class %s is abstract or private. It cannot have an injected constructor.",
                parentClass.qualifiedName?.asString()
            )
            return null
        }

        return parentClass to createConstructorInjectionTarget()
    }

    private fun KSFunctionDeclaration.isValidInjectAnnotatedConstructor(): Boolean {
        val parentClass = parentDeclaration as KSClassDeclaration

        // Verify modifiers.
        if (isPrivate()) {
            logger.error(
                this,
                "@Inject constructors must not be private in class %s.",
                parentClass.qualifiedName?.asString()
            )
            return false
        }

        // Verify parentScope modifiers.
        if (parentClass.isPrivate()) {
            logger.error(
                this,
                "Class %s is private. @Inject constructors are not allowed in private classes.",
                parentClass.qualifiedName?.asString()
            )
            return false
        }

        if (parentClass.isNonStaticInnerClass()) return false

        return parameters.all { param ->
            param.type.resolve().isValidInjectedType(
                node = this,
                qualifiedName = qualifiedName?.asString()
            )
        }
    }

    private fun KSFunctionDeclaration.createConstructorInjectionTarget(): ConstructorInjectionTarget {
        val parentClass = parentDeclaration as KSClassDeclaration
        val scopeName = parentClass.getScopeName()

        parentClass.checkReleasableAnnotationValidity()
        parentClass.checkProvidesReleasableAnnotationValidity()

        if (parentClass.isAnnotationPresent(ProvidesSingleton::class) && scopeName == null) {
            logger.error(
                parentClass,
                "The type %s uses @ProvidesSingleton but doesn't have a scope annotation.",
                parentClass.qualifiedName?.asString()
            )
        }

        return ConstructorInjectionTarget(
            builtClass = parentClass,
            scopeName = scopeName,
            hasSingletonAnnotation = parentClass.isAnnotationPresent(Singleton::class),
            hasReleasableAnnotation = parentClass.isAnnotationPresent(Releasable::class),
            hasProvidesSingletonAnnotation = parentClass.isAnnotationPresent(ProvidesSingleton::class),
            hasProvidesReleasableAnnotation = parentClass.isAnnotationPresent(ProvidesReleasable::class),
            superClassThatNeedsMemberInjection = parentClass.getMostDirectSuperClassWithInjectedMembers(),
            parameters = getParamInjectionTargetList()
        )
    }

    private fun KSClassDeclaration.createConstructorInjectionTarget(): ConstructorInjectionTarget? {
        val scopeName = getScopeName()
        checkReleasableAnnotationValidity()
        checkProvidesReleasableAnnotationValidity()

        if (isAnnotationPresent(ProvidesSingleton::class) && scopeName == null) {
            logger.error(
                this,
                "The type %s uses @ProvidesSingleton but doesn't have a scope annotation.",
                qualifiedName?.asString()
            )
        }

        val constructors = getConstructors()

        // we just need to deal with the case of the default constructor only.
        // like Guice, we will call it by default in the optimistic factory
        // injected constructors will be handled at some point in the compilation cycle

        // if there is an injected constructor, it will be caught later, just leave
        if (constructors.any { element -> element.isAnnotationPresent(Inject::class) }) return null

        val cannotCreateAFactoryMessage = (" Toothpick can't create a factory for it."
            + " If this class is itself a DI entry point (i.e. you call TP.inject(this) at some point), "
            + " then you can remove this warning by adding @SuppressWarnings(\"Injectable\") to the class."
            + " A typical example is a class using injection to assign its fields, that calls TP.inject(this),"
            + " but it needs a parameter for its constructor and this parameter is not injectable.")

        // search for default constructor
        for (constructor in constructors) {
            if (constructor.parameters.isEmpty()) {
                if (constructor.isPrivate()) {
                    if (!isInjectableWarningSuppressed()) {
                        val message = String.format(
                            "The class %s has a private default constructor. $cannotCreateAFactoryMessage",
                            qualifiedName?.asString()
                        )

                        constructor.crashOrWarnWhenNoFactoryCanBeCreated(message)
                    }

                    return null
                }

                return ConstructorInjectionTarget(
                    builtClass = this,
                    scopeName = scopeName,
                    hasSingletonAnnotation = isAnnotationPresent(Singleton::class),
                    hasReleasableAnnotation = isAnnotationPresent(Releasable::class),
                    hasProvidesSingletonAnnotation = isAnnotationPresent(ProvidesSingleton::class),
                    hasProvidesReleasableAnnotation = isAnnotationPresent(ProvidesReleasable::class),
                    superClassThatNeedsMemberInjection = getMostDirectSuperClassWithInjectedMembers()
                )
            }
        }

        if (!isInjectableWarningSuppressed()) {
            crashOrWarnWhenNoFactoryCanBeCreated(
                "The class ${qualifiedName?.asString()} has injected members or a scope annotation but has no " +
                    "@Inject-annotated (non-private) constructor  nor a non-private default constructor. " +
                    cannotCreateAFactoryMessage
            )
        }
        return null
    }

    private fun KSNode.crashOrWarnWhenNoFactoryCanBeCreated(message: String) {
        if (options.crashWhenNoFactoryCanBeCreated) {
            logger.error(this, message)
        } else {
            logger.warn(this, message)
        }
    }

    /**
     * Lookup [javax.inject.Scope] annotated annotations to provide the name of the scope the
     * `typeElement` belongs to. The method logs an error if the `typeElement` has
     * multiple scope annotations.
     *
     * @receiver the element for which a scope is to be found.
     * @return the scope of this `typeElement` or `null` if it has no scope annotations.
     */
    private fun KSAnnotated.getScopeName(): String? {
        var scopeName: String? = null
        var hasScopeAnnotation = false

        annotations
            .mapNotNull { annotationMirror -> annotationMirror.annotationType.resolve().declaration as? KSClassDeclaration }
            .forEach { annotation ->
                val isSingletonAnnotation =
                    annotation.qualifiedName?.asString() == Singleton::class.qualifiedName!!

                if (!isSingletonAnnotation && annotation.isAnnotationPresent(Scope::class)) {
                    annotation.checkScopeAnnotationValidity()
                    if (scopeName != null) {
                        logger.error(this, "Only one @Scope qualified annotation is allowed : %s", scopeName)
                    }
                    scopeName = annotation.qualifiedName?.asString()
                }

                if (isSingletonAnnotation) {
                    hasScopeAnnotation = true
                }
            }

        if (hasScopeAnnotation && scopeName == null) {
            return Singleton::class.qualifiedName!!
        }

        return scopeName
    }

    private fun KSAnnotated.checkReleasableAnnotationValidity() {
        if (isAnnotationPresent(Releasable::class) && !isAnnotationPresent(Singleton::class)) {
            logger.error(
                this,
                "Class %s is annotated with @Releasable, it should also be annotated with @Singleton.",
                (this as? KSDeclaration)?.qualifiedName?.asString()
            )
        }
    }

    private fun KSAnnotated.checkProvidesReleasableAnnotationValidity() {
        if (isAnnotationPresent(ProvidesReleasable::class) && !isAnnotationPresent(ProvidesSingletonInScope::class)) {
            logger.error(
                this,
                "Class %s is annotated with @ProvidesReleasable, it should also be annotated with either @ProvidesSingleton.",
                (this as? KSDeclaration)?.qualifiedName?.asString()
            )
        }
    }

    private fun KSAnnotated.checkScopeAnnotationValidity(): Boolean {
        if (!isAnnotationPresent(Scope::class)) {
            logger.error(
                this,
                "Scope Annotation %s does not contain Scope annotation.",
                (this as? KSDeclaration)?.qualifiedName?.asString()
            )
            return false
        }

        val javaRetention = getAnnotationsByType(java.lang.annotation.Retention::class).firstOrNull()?.value
        val ktRetention = getAnnotationsByType(Retention::class).firstOrNull()?.value

        if (javaRetention != RetentionPolicy.RUNTIME && ktRetention != AnnotationRetention.RUNTIME) {
            logger.error(
                this,
                "Scope Annotation %s does not have RUNTIME retention policy.",
                (this as? KSDeclaration)?.qualifiedName?.asString()
            )
            return false
        }
        return true
    }

    /**
     * Checks if the injectable warning is suppressed for the TypeElement, through the usage
     * of @SuppressWarning("Injectable").
     *
     * @param typeElement the element to check if the warning is suppressed.
     * @return true is the injectable warning is suppressed, false otherwise.
     */
    private fun KSAnnotated.isInjectableWarningSuppressed(): Boolean =
        hasWarningSuppressed(SUPPRESS_WARNING_ANNOTATION_INJECTABLE_VALUE)

    private fun KSClassDeclaration.canTypeHaveAFactory(): Boolean {
        return !isAbstract() && !isPrivate()
    }

    companion object {
        private const val SUPPRESS_WARNING_ANNOTATION_INJECTABLE_VALUE = "injectable"
    }
}