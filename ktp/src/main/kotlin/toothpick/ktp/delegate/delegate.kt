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
package toothpick.ktp.delegate

import kotlin.reflect.KClass

/**
 * @param T the type of the thing to inject, the key of the binding.
 * @return a property delegate that will inject eagerly an instance.
 */
inline fun <reified T : Any> inject() = EagerDelegateProvider(T::class.java)

/**
 * @param T the type of the thing to inject, the key of the binding.
 * @param name a nullable name to designate a specific named binding.
 * @return a property delegate that will inject eagerly an instance.
 */
inline fun <reified T : Any> inject(name: String) = EagerDelegateProvider(T::class.java, name)

/**
 * @param T the type of the thing to inject, the key of the binding.
 * @param name a nullable name to designate a specific named binding.
 * @return a property delegate that will inject eagerly an instance.
 */
inline fun <reified T : Any> inject(name: KClass<out Annotation>) = EagerDelegateProvider(T::class.java, name.java)

/**
 * @param T the type of the thing to inject, the key of the binding.
 * @return a property delegate that will inject a provider.
 */
inline fun <reified T : Any> provider() = ProviderDelegateProvider(T::class.java)

/**
 * @param T the type of the thing to inject, the key of the binding.
 * @param name a nullable name to designate a specific named binding.
 * @return a property delegate that will inject a provider.
 */
inline fun <reified T : Any> provider(name: String) = ProviderDelegateProvider(T::class.java, name)

/**
 * @param T the type of the thing to inject, the key of the binding.
 * @param name a nullable name to designate a specific named binding.
 * @return a property delegate that will inject a provider.
 */
inline fun <reified T : Any> provider(name: KClass<out Annotation>) = ProviderDelegateProvider(T::class.java, name.java)

/**
 * @param T the type of the thing to inject, the key of the binding.
 * @return a property delegate that will inject a lazy.
 */
inline fun <reified T : Any> lazy() = LazyDelegateProvider(T::class.java)

/**
 * @param T the type of the thing to inject, the key of the binding.
 * @param name a nullable name to designate a specific named binding.
 * @return a property delegate that will inject a lazy.
 */
inline fun <reified T : Any> lazy(name: String) = LazyDelegateProvider(T::class.java, name)

/**
 * @param T the type of the thing to inject, the key of the binding.
 * @param name a nullable name to designate a specific named binding.
 * @return a property delegate that will inject a lazy.
 */
inline fun <reified T : Any> lazy(name: KClass<out Annotation>) = LazyDelegateProvider(T::class.java, name.java)
