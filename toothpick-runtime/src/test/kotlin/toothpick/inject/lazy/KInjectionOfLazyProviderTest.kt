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
package toothpick.inject.lazy

import org.hamcrest.CoreMatchers.isA
import org.hamcrest.CoreMatchers.notNullValue
import org.hamcrest.CoreMatchers.sameInstance
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test
import toothpick.Lazy
import toothpick.ScopeImpl
import toothpick.Toothpick
import toothpick.config.Module
import toothpick.data.KBar
import toothpick.data.KFooWithLazy
import toothpick.data.KFooWithNamedLazy

/*
 * Test injection of {@code Lazy}s.
 */
class KInjectionOfLazyProviderTest {

    @Test
    @Throws(Exception::class)
    fun testSimpleInjection() {
        // GIVEN
        val scope = ScopeImpl("")
        val fooWithLazy = KFooWithLazy()

        // WHEN
        Toothpick.inject(fooWithLazy, scope)

        // THEN
        assertThat(fooWithLazy.bar, notNullValue())
        assertThat(fooWithLazy.bar, isA(Lazy::class.java))
        val bar1 = fooWithLazy.bar.get()
        assertThat(bar1, isA(KBar::class.java))
        val bar2 = fooWithLazy.bar.get()
        assertThat(bar2, isA(KBar::class.java))
        assertThat(bar2, sameInstance(bar1))
    }

    @Test
    @Throws(Exception::class)
    fun testNamedInjection() {
        // GIVEN
        val scope = ScopeImpl("")
        scope.installModules(object : Module() {
            init {
                bind(KBar::class.java).withName("foo").to(KBar::class.java)
            }
        })
        val fooWithLazy = KFooWithNamedLazy()

        // WHEN
        Toothpick.inject(fooWithLazy, scope)

        // THEN
        assertThat(fooWithLazy.bar, notNullValue())
        assertThat(fooWithLazy.bar, isA(Lazy::class.java))
        val bar1 = fooWithLazy.bar.get()
        assertThat(bar1, isA(KBar::class.java))
        val bar2 = fooWithLazy.bar.get()
        assertThat(bar2, isA(KBar::class.java))
        assertThat(bar2, sameInstance(bar1))
    }

    @Test(expected = IllegalStateException::class)
    @Throws(Exception::class)
    fun testLazyAfterClosingScope() {
        // GIVEN
        val scopeName = ""
        val fooWithLazy = KFooWithLazy()

        // WHEN
        Toothpick.inject(fooWithLazy, Toothpick.openScope(scopeName))
        Toothpick.closeScope(scopeName)
        System.gc()

        // THEN
        fooWithLazy.bar.get() // should crash
    }
}
