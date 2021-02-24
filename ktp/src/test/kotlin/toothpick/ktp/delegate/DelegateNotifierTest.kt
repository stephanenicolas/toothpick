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

import com.nhaarman.mockitokotlin2.mock
import org.amshove.kluent.Verify
import org.amshove.kluent.VerifyNoFurtherInteractions
import org.amshove.kluent.VerifyNoInteractions
import org.amshove.kluent.called
import org.amshove.kluent.on
import org.amshove.kluent.that
import org.amshove.kluent.was
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import toothpick.ScopeImpl

class DelegateNotifierTest {

    @Nested
    inner class SingleContainer {

        @Test
        fun `notifyDelegates should notify registered delegates`() {
            // GIVEN
            val container = Container()
            val scope = ScopeImpl("name")
            val delegate1: InjectDelegate<Any> = mock()
            val delegate2: InjectDelegate<Any> = mock()

            val delegateNotifier = DelegateNotifier().apply {
                registerDelegate(container, delegate1)
                registerDelegate(container, delegate2)
            }

            // WHEN
            delegateNotifier.notifyDelegates(container, scope)

            // THEN
            Verify on delegate1 that delegate1.onEntryPointInjected(scope) was called
            Verify on delegate2 that delegate2.onEntryPointInjected(scope) was called
        }

        @Test
        fun `notifyDelegates should not notify delegates a second time`() {
            // GIVEN
            val container = Container()
            val scope = ScopeImpl("name")
            val delegate1: InjectDelegate<Any> = mock()

            val delegateNotifier = DelegateNotifier().apply {
                registerDelegate(container, delegate1)
            }

            // WHEN
            delegateNotifier.notifyDelegates(container, scope)
            delegateNotifier.notifyDelegates(container, scope)

            // THEN
            Verify on delegate1 that delegate1.onEntryPointInjected(scope) was called
            VerifyNoFurtherInteractions on delegate1
        }

        @Test
        fun `notifyDelegates should neither notify nor fail if not delegate is registered`() {
            // GIVEN
            val container = Container()
            val scope = ScopeImpl("name")
            val delegateNotifier = DelegateNotifier()

            // WHEN
            delegateNotifier.notifyDelegates(container, scope)

            // THEN
            // should not fail
        }
    }

    @Nested
    inner class MultipleContainer {

        @Test
        fun `notifyDelegates should notify only registered delegates for the notified container`() {
            // GIVEN
            val container1 = Container()
            val container2 = Container()
            val scope = ScopeImpl("name")
            val delegate1: InjectDelegate<Any> = mock()
            val delegate2: InjectDelegate<Any> = mock()

            val delegateNotifier = DelegateNotifier().apply {
                registerDelegate(container1, delegate1)
                registerDelegate(container2, delegate2)
            }

            // WHEN
            delegateNotifier.notifyDelegates(container1, scope)

            // THEN
            Verify on delegate1 that delegate1.onEntryPointInjected(scope) was called
            VerifyNoInteractions on delegate2
        }

        @Test
        fun `notifyDelegates should notify both registered delegates when both containers are notified`() {
            // GIVEN
            val container1 = Container()
            val container2 = Container()
            val scope = ScopeImpl("name")
            val delegate1: InjectDelegate<Any> = mock()
            val delegate2: InjectDelegate<Any> = mock()

            val delegateNotifier = DelegateNotifier().apply {
                registerDelegate(container1, delegate1)
                registerDelegate(container2, delegate2)
            }

            // WHEN
            delegateNotifier.notifyDelegates(container1, scope)
            delegateNotifier.notifyDelegates(container2, scope)

            // THEN
            Verify on delegate1 that delegate1.onEntryPointInjected(scope) was called
            Verify on delegate2 that delegate2.onEntryPointInjected(scope) was called
        }
    }

    class Container
}
