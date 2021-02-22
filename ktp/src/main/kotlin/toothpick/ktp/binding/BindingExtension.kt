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
package toothpick.ktp.binding

import javax.inject.Provider
import kotlin.reflect.KClass
import toothpick.config.Binding
import toothpick.config.Module

/**
 * DSL method to define bindings. A typical example is
 * <pre>
 * module {
 *   bind(Foo::class)...
 *   bind(bar::class)...
 *   ...
 * }
 * </pre>
 * @param bindings a list of bindings statements.
 * @return a module containing all the bindings {@code bindings}.
 */
fun module(bindings: Module.() -> Unit): Module = Module().apply { bindings() }

/**
 * DSL method to start a binding statement.
 * @param key the {@link KClass} used as a key for this binding.
 * @return a binding statement for this key, that can be further customized.
 * @see module(Module.() -> Unit)
 */
fun <T : Any> Module.bind(key: KClass<T>): CanBeNamed<T> = CanBeNamed(bind(key.java))

/**
 * DSL method to start a binding statement, using reified types for a more Kotlin friendly syntax.
 * @param T the {@link KClass} used as a key for this binding.
 * @return a binding statement for this key, that can be further customized.
 * @see toothpick.config.Module.bind(Class)
 */
inline fun <reified T : Any> Module.bind(): CanBeNamed<T> = CanBeNamed(bind(T::class.java))

open class CanBeBound<T : Any>(open val delegate: Binding<T>.CanBeBound) {
    /**
     * DSL method to make a binding Singleton.
     * @return a binding statement for this key, with the implementation class {@code implClass}
     * that can be further customized.
     */
    fun singleton(): Binding<T>.CanBeReleasable = delegate.singleton()

    /**
     * DSL method to associate an implementation class to a binding.
     * @param implClass the {@link KClass} used as an implementation class for this binding.
     * @return a binding statement for this key, with the implementation class {@code implClass}
     * that can be further customized.
     */
    fun toClass(implClass: KClass<out T>): Binding<T>.CanBeSingleton = delegate.to(implClass.java)

    /**
     * DSL method to associate an implementation class to a binding, more Kotlin friendly.
     * @return a binding statement for this key, with the implementation class {@code T}
     * that can be further customized.
     * @see Binding.CanBeBound.toClass(KClass<T>)
     */
    inline fun <reified P : T> toClass(): Binding<T>.CanBeSingleton = delegate.to(P::class.java)

    /**
     * DSL method to associate an instance to a binding, more Kotlin friendly.
     * @param instance to be used
     * @return a binding statement for this key, with the instance provided by the lambda
     * that can be further customized.
     */
    fun toInstance(instance: T) = delegate.toInstance(instance)

    /**
     * DSL method to associate an instance to a binding, more Kotlin friendly.
     * @param instanceProvider a lambda that creates the instance
     * @return a binding statement for this key, with the instance provided by the lambda
     * that can be further customized.
     */
    fun toInstance(instanceProvider: () -> T) = delegate.toInstance(instanceProvider())

    /**
     * DSL method to associate an provider to a binding, more Kotlin friendly.
     * @param providerClass the {@link KClass} used as a provider class for this binding.
     * @return a binding statement for this key, with the provider class
     * that can be further customized.
     */
    fun toProvider(providerClass: KClass<out Provider<out T>>): Binding<T>.CanProvideSingletonOrSingleton = delegate.toProvider(providerClass.java)

    // https://youtrack.jetbrains.com/issue/KT-17061
    // inline fun <T, reified P : Provider<out T>> Binding<T>.CanBeBound.toProvider(): Binding<T>.CanProvideSingletonOrSingleton = toProvider(P::class.java)

    /**
     * DSL method to associate an provider impl to a binding, more Kotlin friendly.
     * @param providerInstance the provider instance to be used for this binding.
     * @return a binding statement for this key, with the provider class
     * that can be further customized.
     */
    fun toProviderInstance(providerInstance: Provider<out T>): Binding<T>.CanProvideSingleton = delegate.toProviderInstance(providerInstance)

    /**
     * DSL method to associate an provider impl to a binding, more Kotlin friendly.
     * @param providerInstanceProvider a lambda used as a provider for this binding.
     * @return a binding statement for this key, with the provider class
     * that can be further customized.
     */
    fun toProviderInstance(providerInstanceProvider: () -> T): Binding<T>.CanProvideSingleton = delegate.toProviderInstance(providerInstanceProvider)
}

class CanBeNamed<T : Any>(override val delegate: Binding<T>.CanBeNamed) : CanBeBound<T>(delegate) {
    /**
     * DSL method to give a name (String) to a binding.
     * @param name the {@link String} used as a name for this binding.
     * @return a binding statement for this key, with the name {@code name}
     * that can be further customized.
     */
    fun withName(name: String): CanBeBound<T> = CanBeBound(delegate.withName(name))

    /**
     * DSL method to give a name (annotation class) to a binding.
     * @param annotationClassWithQualifierAnnotation the {@link KClass} used as a name for this binding.
     * @return a binding statement for this key, with the name {@code annotationClassWithQualifierAnnotation}
     * that can be further customized.
     */
    fun withName(annotationClassWithQualifierAnnotation: KClass<out Annotation>): CanBeBound<T> = CanBeBound(delegate.withName(annotationClassWithQualifierAnnotation.java))

    // https://youtrack.jetbrains.com/issue/KT-17061
    // inline fun <T, reified A : Annotation> Binding<T>.CanBeNamed.withName() = withName(A::class.java)
}
