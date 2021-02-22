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

import java.util.Collections
import java.util.WeakHashMap
import toothpick.Scope

/**
 * Internal KTP class that registers field injection delegates.
 * The fields will be injected when calling {@link Scope#inject(Any)} with the object/entry point
 * using field injection.
 */
class DelegateNotifier {

    private val delegatesMap: MutableMap<Any, MutableList<InjectDelegate<out Any>>> = Collections.synchronizedMap(WeakHashMap())

    /**
     * Registers a delegate for a field injection.
     * @param container the entry point that is using field injection.
     * @param delegate the delegate for this field.
     */
    fun registerDelegate(container: Any, delegate: InjectDelegate<out Any>) {
        val delegates = delegatesMap.getOrPut(container) { mutableListOf() }
        delegates.add(delegate)
    }

    /**
     * Notify delegates that they should perform field injection.
     * @param container the entry point that is using field injection.
     * @param scope the scope used to inject the fields.
     */
    fun notifyDelegates(container: Any, scope: Scope) {
        delegatesMap[container]?.forEach {
            it.onEntryPointInjected(scope)
        }
        delegatesMap.remove(container)
    }

    /**
     * @return whether or not an entry point has delegates associated to it.
     */
    fun hasDelegates(container: Any): Boolean {
        return delegatesMap.contains(container)
    }
}
