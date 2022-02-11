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
package toothpick.compiler.factory

import com.google.devtools.ksp.KspExperimental
import com.google.devtools.ksp.getAnnotationsByType
import com.google.devtools.ksp.getClassDeclarationByName
import com.google.devtools.ksp.getConstructors
import com.google.devtools.ksp.isAbstract
import com.google.devtools.ksp.isAnnotationPresent
import com.google.devtools.ksp.isConstructor
import com.google.devtools.ksp.isPrivate
import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.symbol.FunctionKind
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.KSName
import com.google.devtools.ksp.symbol.KSNode
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.google.devtools.ksp.symbol.Modifier
import toothpick.Factory
import toothpick.InjectConstructor
import toothpick.ProvidesReleasable
import toothpick.ProvidesSingleton
import toothpick.ProvidesSingletonInScope
import toothpick.Releasable
import toothpick.compiler.common.ToothpickProcessor
import toothpick.compiler.common.generators.error
import toothpick.compiler.common.generators.info
import toothpick.compiler.common.generators.warn
import toothpick.compiler.factory.generators.FactoryGenerator
import toothpick.compiler.factory.targets.ConstructorInjectionTarget
import java.lang.annotation.RetentionPolicy
import javax.inject.Inject
import javax.inject.Scope
import javax.inject.Singleton

/**
 * This processor's role is to create [Factory] classes for injected classes.
 *
 * We create factories in different situations:
 *
 *  * When a class `Foo` has an [javax.inject.Inject] annotated constructor:
 *    * --> we create a Factory to create `Foo` instances.
 *
 * The processor will also try to relax the constraints to generate factories in a few cases. These
 * factories are helpful as they require less work from developers:
 *
 *  * When a class `Foo` is annotated with [javax.inject.Singleton]:
 *    * --> it will use the annotated constructor or the default constructor if possible.
 *    Otherwise, an error is raised.
 *  * When a class `Foo` is annotated with [ProvidesSingleton]:
 *    * --> it will use the annotated constructor or the default constructor if possible.
 *    Otherwise, an error is raised.
 *  * When a class `Foo` has an [javax.inject.Inject] annotated field `@Inject B b`:
 *    * --> it will use the annotated constructor or the default constructor if possible.
 *    Otherwise, an error is raised.
 *  * When a class `Foo` has an [javax.inject.Inject] method `@Inject m()`:
 *    * --> it will use the annotated constructor or the default constructor if possible. Otherwise
 * an error is raised.
 *
 * Note that if a class is abstract, the relax mechanism doesn't generate a factory and raises no error.
 */
@OptIn(KspExperimental::class)
class FactoryProcessor(
    processorOptions: Map<String, String>,
    codeGenerator: CodeGenerator,
    logger: KSPLogger
) : ToothpickProcessor(
    processorOptions, codeGenerator, logger
) {
    override fun process(resolver: Resolver): List<KSAnnotated> {
        val supportedAnnotationTypes: Set<KSClassDeclaration> =
            options.supportedAnnotationTypes
                .mapNotNull { className -> resolver.getClassDeclarationByName(className) }
                .toSet()

        createFactoriesForClassesAnnotatedWithInjectConstructor(resolver)
            .plus(createFactoriesForClassesWithInjectAnnotatedConstructors(resolver))
            .plus(createFactoriesForClassesAnnotatedWith(resolver, ProvidesSingleton::class.qualifiedName!!))
            .plus(createFactoriesForClassesWithInjectAnnotatedFields(resolver))
            .plus(createFactoriesForClassesWithInjectAnnotatedMethods(resolver))
            .plus(createFactoriesForClassesAnnotatedWithScopeAnnotations(resolver, supportedAnnotationTypes))
            .distinctBy { target -> target.sourceClass }
            .forEach { target ->
                val factoryGenerator = FactoryGenerator(target)
                writeToFile(
                    tpCodeGenerator = factoryGenerator,
                    fileDescription = "Factory for type ${target.sourceClass.qualifiedName?.asString()}"
                )

                if (options.verboseLogging) {
                    logger.info(
                        "%s generated class %s",
                        factoryGenerator.sourceClassName.toString(),
                        factoryGenerator.generatedClassName.toString()
                    )
                }
            }

        return emptyList()
    }

    private fun createFactoriesForClassesAnnotatedWithScopeAnnotations(
        resolver: Resolver,
        annotations: Set<KSClassDeclaration>
    ): Sequence<ConstructorInjectionTarget> {
        return annotations.asSequence()
            .filter { annotation -> annotation.isAnnotationPresent(Scope::class) }
            .filter { annotation -> annotation.checkScopeAnnotationValidity() }
            .mapNotNull { annotation -> annotation.qualifiedName?.asString() }
            .flatMap { annotationName -> createFactoriesForClassesAnnotatedWith(resolver, annotationName) }
    }

    private fun createFactoriesForClassesWithInjectAnnotatedMethods(resolver: Resolver): Sequence<ConstructorInjectionTarget> {
        return resolver.getSymbolsWithAnnotation(Inject::class.qualifiedName!!)
            .filterIsInstance<KSFunctionDeclaration>()
            .filter { function -> function.functionKind == FunctionKind.MEMBER }
            .mapNotNull { function -> function.getParentClassOrNull() }
            .distinct()
            .mapNotNull { parentClass -> parentClass.processClassContainingInjectAnnotatedMember(resolver) }
    }

    private fun createFactoriesForClassesWithInjectAnnotatedFields(resolver: Resolver): Sequence<ConstructorInjectionTarget> {
        return resolver.getSymbolsWithAnnotation(Inject::class.qualifiedName!!)
            .filterIsInstance<KSPropertyDeclaration>()
            .mapNotNull { property -> property.getParentClassOrNull() }
            .distinct()
            .mapNotNull { parentClass -> parentClass.processClassContainingInjectAnnotatedMember(resolver) }
    }

    private fun createFactoriesForClassesAnnotatedWith(
        resolver: Resolver,
        annotationName: String
    ): Sequence<ConstructorInjectionTarget> {
        return resolver.getSymbolsWithAnnotation(annotationName)
            .filterIsInstance<KSClassDeclaration>()
            .mapNotNull { annotatedClass -> annotatedClass.processClassContainingInjectAnnotatedMember(resolver) }
    }

    private fun createFactoriesForClassesWithInjectAnnotatedConstructors(resolver: Resolver): Sequence<ConstructorInjectionTarget> {
        return resolver.getSymbolsWithAnnotation(Inject::class.qualifiedName!!)
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

                constructor.processInjectAnnotatedConstructor(resolver)
            }
    }

    private fun createFactoriesForClassesAnnotatedWithInjectConstructor(resolver: Resolver): Sequence<ConstructorInjectionTarget> {
        return resolver.getSymbolsWithAnnotation(InjectConstructor::class.qualifiedName!!)
            .filterIsInstance<KSClassDeclaration>()
            .mapNotNull { annotatedClass ->
                val constructors = annotatedClass.getConstructors()
                val firstConstructor = constructors.firstOrNull()

                if (constructors.count() == 1 &&
                    firstConstructor != null &&
                    !firstConstructor.isAnnotationPresent(Inject::class)
                ) {
                    firstConstructor.processInjectAnnotatedConstructor(resolver)
                } else {
                    logger.error(
                        constructors.firstOrNull(),
                        "Class %s is annotated with @InjectConstructor. Therefore, It must have one unique constructor and it should not be annotated with @Inject.",
                        annotatedClass.qualifiedName?.asString()
                    )
                    null
                }
            }
    }

    private fun KSClassDeclaration.processClassContainingInjectAnnotatedMember(resolver: Resolver): ConstructorInjectionTarget? {
        if (isExcludedByFilters()) return null
        if (!canTypeHaveAFactory()) return null
        return createConstructorInjectionTarget(resolver)
    }

    private fun KSFunctionDeclaration.isSingleInjectAnnotatedConstructor(): Boolean {
        return (parentDeclaration as KSClassDeclaration)
            .getConstructors()
            .all { constructor ->
                constructor == this || !constructor.isAnnotationPresent(Inject::class)
            }
    }

    private fun KSFunctionDeclaration.processInjectAnnotatedConstructor(resolver: Resolver): ConstructorInjectionTarget? {
        val parentClass = parentDeclaration as KSClassDeclaration

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

        return createConstructorInjectionTarget(resolver)
    }

    private fun KSFunctionDeclaration.isValidInjectAnnotatedConstructor(): Boolean {
        val parentClass = parentDeclaration as KSClassDeclaration

        if (isPrivate()) {
            logger.error(
                this,
                "@Inject constructors must not be private in class %s.",
                parentClass.qualifiedName?.asString()
            )
            return false
        }

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
                qualifiedName = param.name?.asString()
            )
        }
    }

    private fun KSFunctionDeclaration.createConstructorInjectionTarget(resolver: Resolver): ConstructorInjectionTarget {
        val parentClass = parentDeclaration as KSClassDeclaration
        val scopeName = parentClass.getScopeName(resolver)

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
            sourceClass = parentClass,
            scopeName = scopeName,
            hasSingletonAnnotation = parentClass.isAnnotationPresent(Singleton::class),
            hasReleasableAnnotation = parentClass.isAnnotationPresent(Releasable::class),
            hasProvidesSingletonAnnotation = parentClass.isAnnotationPresent(ProvidesSingleton::class),
            hasProvidesReleasableAnnotation = parentClass.isAnnotationPresent(ProvidesReleasable::class),
            superClassThatNeedsMemberInjection = parentClass.getMostDirectSuperClassWithInjectedMembers(onlyParents = false),
            parameters = getParamInjectionTargetList()
        )
    }

    private fun KSClassDeclaration.createConstructorInjectionTarget(resolver: Resolver): ConstructorInjectionTarget? {
        val scopeName = getScopeName(resolver)
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
        if (constructors.any { constructor -> constructor.isAnnotationPresent(Inject::class) }) return null

        val cannotCreateAFactoryMessage = (
            " Toothpick can't create a factory for it." +
                " If this class is itself a DI entry point (i.e. you call TP.inject(this) at some point), " +
                " then you can remove this warning by adding @Suppress(\"Injectable\") to the class." +
                " A typical example is a class using injection to assign its fields, that calls TP.inject(this)," +
                " but it needs a parameter for its constructor and this parameter is not injectable."
            )

        val defaultConstructor = constructors.firstOrNull { constructor ->
            constructor.parameters.isEmpty()
        }

        if (defaultConstructor == null) {
            if (!isInjectableWarningSuppressed()) {
                crashOrWarnWhenNoFactoryCanBeCreated(
                    this,
                    "The class %s has injected members or a scope annotation but has no @Inject-annotated (non-private) constructor nor a non-private default constructor. %s",
                    qualifiedName?.asString(),
                    cannotCreateAFactoryMessage
                )
            }

            return null
        }

        if (defaultConstructor.isPrivate()) {
            if (!isInjectableWarningSuppressed()) {
                crashOrWarnWhenNoFactoryCanBeCreated(
                    this,
                    "The class %s has a private default constructor. %s",
                    qualifiedName?.asString(),
                    cannotCreateAFactoryMessage
                )
            }

            return null
        }

        return ConstructorInjectionTarget(
            sourceClass = this,
            scopeName = scopeName,
            hasSingletonAnnotation = isAnnotationPresent(Singleton::class),
            hasReleasableAnnotation = isAnnotationPresent(Releasable::class),
            hasProvidesSingletonAnnotation = isAnnotationPresent(ProvidesSingleton::class),
            hasProvidesReleasableAnnotation = isAnnotationPresent(ProvidesReleasable::class),
            superClassThatNeedsMemberInjection = getMostDirectSuperClassWithInjectedMembers(onlyParents = false)
        )
    }

    private fun crashOrWarnWhenNoFactoryCanBeCreated(node: KSNode, message: String, vararg args: Any?) {
        if (options.crashWhenNoFactoryCanBeCreated) logger.error(node, message, *args)
        else logger.warn(node, message, *args)
    }

    /**
     * Lookup [javax.inject.Scope] annotated annotations to provide the name of the scope the
     * receiver belongs to. The method logs an error if the receiver has multiple scope annotations.
     *
     * @receiver the node for which a scope is to be found.
     * @return the scope of this node or `null` if it has no scope annotations.
     */
    private fun KSAnnotated.getScopeName(resolver: Resolver): KSName? {
        var scopeName: KSName? = null
        var hasScopeAnnotation = false

        annotations
            .mapNotNull { annotationMirror -> annotationMirror.annotationType.resolve().declaration as? KSClassDeclaration }
            .forEach { annotation ->
                val isSingletonAnnotation =
                    annotation.qualifiedName?.asString() == Singleton::class.qualifiedName!!

                if (!isSingletonAnnotation && annotation.isAnnotationPresent(Scope::class)) {
                    annotation.checkScopeAnnotationValidity()
                    if (scopeName != null) {
                        logger.error(this, "Only one @Scope qualified annotation is allowed: %s", scopeName)
                    }
                    scopeName = annotation.qualifiedName
                }

                if (isSingletonAnnotation) {
                    hasScopeAnnotation = true
                }
            }

        if (hasScopeAnnotation && scopeName == null) {
            return resolver.getKSNameFromString(Singleton::class.qualifiedName!!)
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

    private fun KSClassDeclaration.isNonStaticInnerClass(): Boolean {
        if (modifiers.contains(Modifier.INNER)) {
            logger.error(
                this,
                "Class %s is a non static inner class. @Inject constructors are not allowed in non static inner classes.",
                qualifiedName?.asString()
            )
            return true
        }
        return false
    }

    /**
     * Checks if the injectable warning is suppressed for the receiver, through the usage
     * of @SuppressWarning("Injectable").
     *
     * @receiver the node to check if the warning is suppressed.
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
