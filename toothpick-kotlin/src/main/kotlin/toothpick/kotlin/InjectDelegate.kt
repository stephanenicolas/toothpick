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
package com.example.toothpick.ktp

import toothpick.Scope
import java.lang.IllegalStateException
import kotlin.reflect.KProperty

class InjectDelegate<T : Any>(private val clz: Class<T>, private val name: String?) {

    lateinit var instance: T

    fun onEntryPointInjected(scope: Scope) {
        instance = scope.getInstance(clz, name)
    }

    operator fun getValue(thisRef: Any, property: KProperty<*>): T {
        if (!this::instance.isInitialized) {
            throw IllegalStateException("The dependency has not be injected yet.")
        }
        return instance
    }
}