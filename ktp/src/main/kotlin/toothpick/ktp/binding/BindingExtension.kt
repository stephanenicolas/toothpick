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

import toothpick.config.Binding
import toothpick.config.Module
import javax.inject.Provider
import kotlin.reflect.KClass

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
fun <T : Any> Module.bind(key: KClass<T>): Binding<T>.CanBeNamed = bind(key.java)

/**
 * DSL method to start a binding statement, using reified types for a more Kotlin friendly syntax.
 * @param T the {@link KClass} used as a key for this binding.
 * @return a binding statement for this key, that can be further customized.
 * @see toothpick.config.Module.bind(Class)
 */
inline fun <reified T> Module.bind(): Binding<T>.CanBeNamed = bind(T::class.java)

/**
 * DSL method to give a name (annotation class) to a binding.
 * @param T the {@link KClass} used as a key for this binding.
 * @param annotationClassWithQualifierAnnotation the {@link KClass} used as a name for this binding.
 * @return a binding statement for this key, with the name {@code annotationClassWithQualifierAnnotation}
 * that can be further customized.
 */
infix fun <T : Any> Binding<T>.CanBeNamed.withName(annotationClassWithQualifierAnnotation: KClass<out Annotation>): Binding<T>.CanBeBound = withName(annotationClassWithQualifierAnnotation.java)

// https://youtrack.jetbrains.com/issue/KT-17061
// inline fun <T, reified A : Annotation> Binding<T>.CanBeNamed.withName() = withName(A::class.java)

/**
 * DSL method to associate an implementation class to a binding.
 * @param T the {@link KClass} used as a key for this binding.
 * @param implClass the {@link KClass} used as an implementation class for this binding.
 * @return a binding statement for this key, with the implementation class {@code implClass}
 * that can be further customized.
 */
infix fun <T : Any> Binding<T>.CanBeBound.toClass(implClass: KClass<out T>): Binding<T>.CanBeSingleton = to(implClass.java)

/**
 * DSL method to associate an implementation class to a binding, more Kotlin friendly.
 * @param T the {@link KClass} used as an implementation class for this binding.
 * @return a binding statement for this key, with the implementation class {@code T}
 * that can be further customized.
 * @see Binding.CanBeBound.toClass(KClass<T>)
 */
inline fun <reified T> Binding<in T>.CanBeBound.toClass(): Binding<in T>.CanBeSingleton = to(T::class.java)

/**
 * DSL method to associate an instance to a binding, more Kotlin friendly.
 * @param T the {@link KClass} used as a key for this binding.
 * @param instanceProvider a lambda that creates the instance
 * @return a binding statement for this key, with the instance provided by the lambda
 * that can be further customized.
 */
fun <T> Binding<T>.CanBeBound.toInstance(instanceProvider: () -> T) = toInstance(instanceProvider())

/**
 * DSL method to associate an implto a binding, more Kotlin friendly.
 * @param T the {@link KClass} used as a key for this binding.
 * @param providerClass the {@link KClass} used as a provider class for this binding.
 * @return a binding statement for this key, with the provider class
 * that can be further customized.
 */
fun <T : Any> Binding<T>.CanBeBound.toProvider(providerClass: KClass<out Provider<out T>>): Binding<T>.CanProvideSingletonOrSingleton = toProvider(providerClass.java)
// https://youtrack.jetbrains.com/issue/KT-17061
// inline fun <T, reified P : Provider<out T>> Binding<T>.CanBeBound.toProvider(): Binding<T>.CanProvideSingletonOrSingleton = toProvider(P::class.java)
