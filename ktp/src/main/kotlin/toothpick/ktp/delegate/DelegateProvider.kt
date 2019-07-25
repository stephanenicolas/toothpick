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

import toothpick.ktp.KTP
import kotlin.reflect.KProperty

class DelegateProvider<T : Any>(private val clz: Class<T>, private val name: String?, private val injectionType: InjectionType) {

    operator fun provideDelegate(thisRef: Any, prop: KProperty<*>): InjectDelegate<T> {
        val delegate = createDelegate()
        KTP.delegateNotifier.registerDelegate(thisRef, delegate)
        return delegate
    }

    private fun createDelegate() = when (injectionType) {
        InjectionType.EAGER -> EagerDelegate(clz, name)
        InjectionType.LAZY -> ProviderDelegate(clz, name, true)
        InjectionType.PROVIDER -> ProviderDelegate(clz, name, false)
    }
}
