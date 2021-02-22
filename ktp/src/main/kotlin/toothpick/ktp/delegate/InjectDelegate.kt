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

import javax.inject.Provider
import kotlin.reflect.KProperty
import toothpick.Scope

/**
 * Property delegate base class. It allows to delegate the creation of fields to KTP.
 * A typical example is
 */
sealed class InjectDelegate<T : Any>(protected val clz: Class<T>, protected val name: String?) {

    abstract val instance: T

    abstract fun onEntryPointInjected(scope: Scope)
    abstract fun isEntryPointInjected(): Boolean

    operator fun getValue(thisRef: Any, property: KProperty<*>): T {
        if (!isEntryPointInjected()) {
            throw IllegalStateException("The dependency has not be injected yet.")
        }
        return instance
    }
}

/**
 * Delegate for fields that should be injected eagerly.
 */
class EagerDelegate<T : Any>(clz: Class<T>, name: String?) : InjectDelegate<T>(clz, name) {

    override lateinit var instance: T

    override fun onEntryPointInjected(scope: Scope) {
        instance = scope.getInstance(clz, name)
    }

    override fun isEntryPointInjected() = this::instance.isInitialized
}

/**
 * Delegate for fields that should be injected as a provider.
 */
class ProviderDelegate<T : Any>(clz: Class<T>, name: String?) : InjectDelegate<T>(clz, name) {

    lateinit var provider: Provider<T>

    override val instance: T
        get() = provider.get()

    override fun onEntryPointInjected(scope: Scope) {
        provider = scope.getProvider(clz, name)
    }

    override fun isEntryPointInjected() = this::provider.isInitialized
}

/**
 * Delegate for fields that should be injected lazily.
 */
class LazyDelegate<T : Any>(clz: Class<T>, name: String?) : InjectDelegate<T>(clz, name) {

    lateinit var provider: Provider<T>

    override val instance: T
        get() = provider.get()

    override fun onEntryPointInjected(scope: Scope) {
        provider = scope.getLazy(clz, name)
    }

    override fun isEntryPointInjected() = this::provider.isInitialized
}
