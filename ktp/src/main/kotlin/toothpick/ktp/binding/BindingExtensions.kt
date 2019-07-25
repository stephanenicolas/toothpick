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

// module
fun module(bindings: Module.() -> Unit): Module = Module().apply { bindings() }

// bind
fun <T : Any> Module.bind(key: KClass<T>): Binding<T>.CanBeNamed = bind(key.java)
inline fun <reified T> Module.bind(): Binding<T>.CanBeNamed = bind(T::class.java)

// withName
fun <T : Any> Binding<T>.CanBeNamed.withName(annotationClassWithQualifierAnnotation: KClass<out Annotation>): Binding<T>.CanBeBound = withName(annotationClassWithQualifierAnnotation.java)
// https://youtrack.jetbrains.com/issue/KT-17061
// inline fun <T, reified A : Annotation> Binding<T>.CanBeNamed.withName() = withName(A::class.java)

// toClass
fun <T : Any> Binding<T>.CanBeBound.toClass(implClass: KClass<out T>): Binding<T>.CanBeSingleton = to(implClass.java)
inline fun <reified T> Binding<in T>.CanBeBound.toClass(): Binding<in T>.CanBeSingleton = to(T::class.java)

// toInstance
fun <T> Binding<T>.CanBeBound.toInstance(instanceProvider: () -> T) = toInstance(instanceProvider())

// toProvider
fun <T : Any> Binding<T>.CanBeBound.toProvider(providerClass: KClass<out Provider<out T>>): Binding<T>.CanProvideSingletonOrSingleton = toProvider(providerClass.java)
// https://youtrack.jetbrains.com/issue/KT-17061
// inline fun <T, reified P : Provider<out T>> Binding<T>.CanBeBound.toProvider(): Binding<T>.CanProvideSingletonOrSingleton = toProvider(P::class.java)
