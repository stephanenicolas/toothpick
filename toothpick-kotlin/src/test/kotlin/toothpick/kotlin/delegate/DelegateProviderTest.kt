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
package toothpick.kotlin.delegate

import com.nhaarman.mockitokotlin2.mock
import org.amshove.kluent.shouldBeInstanceOf
import org.junit.jupiter.api.Test

class DelegateProviderTest {

    @Test
    fun `provideDelegate should provide an EagerDelegate for the type EAGER`() {
        // GIVEN
        val delegateProvider = DelegateProvider(MyClass::class.java, "name", InjectionType.EAGER)

        // WHEN
        val delegate = delegateProvider.provideDelegate(this, mock())

        // THEN
        delegate shouldBeInstanceOf EagerDelegate::class
    }

    @Test
    fun `provideDelegate should provide an ProviderDelegate for the type LAZY`() {
        // GIVEN
        val delegateProvider = DelegateProvider(MyClass::class.java, "name", InjectionType.LAZY)

        // WHEN
        val delegate = delegateProvider.provideDelegate(this, mock())

        // THEN
        delegate shouldBeInstanceOf ProviderDelegate::class
    }

    @Test
    fun `provideDelegate should provide an ProviderDelegate for the type PROVIDER`() {
        // GIVEN
        val delegateProvider = DelegateProvider(MyClass::class.java, "name", InjectionType.PROVIDER)

        // WHEN
        val delegate = delegateProvider.provideDelegate(this, mock())

        // THEN
        delegate shouldBeInstanceOf ProviderDelegate::class
    }

    class MyClass
}