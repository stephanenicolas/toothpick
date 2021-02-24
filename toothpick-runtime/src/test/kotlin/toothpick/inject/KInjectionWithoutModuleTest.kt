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
package toothpick.inject

import org.hamcrest.CoreMatchers.isA
import org.hamcrest.CoreMatchers.not
import org.hamcrest.CoreMatchers.notNullValue
import org.hamcrest.CoreMatchers.sameInstance
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test
import toothpick.ScopeImpl
import toothpick.Toothpick
import toothpick.config.Module
import toothpick.data.KBar
import toothpick.data.KFoo
import toothpick.data.KFooChildMaskingMember
import toothpick.data.KFooNested

class KInjectionWithoutModuleTest {

    @Test
    fun testSimpleInjection() {
        // GIVEN
        val scope = ScopeImpl("")
        val foo = KFoo()

        // WHEN
        Toothpick.inject(foo, scope)

        // THEN
        assertThat(foo.bar, notNullValue())
        assertThat(foo.bar, isA(KBar::class.java))
    }

    @Test
    fun testNestedClassInjection() {
        // GIVEN
        val scope = ScopeImpl("")

        // WHEN
        val fooNested = scope.getInstance(KFooNested::class.java)
        val innerClass1 = scope.getInstance(KFooNested.InnerClass1::class.java)
        val innerClass2 = scope.getInstance(KFooNested.InnerClass1.InnerClass2::class.java)

        // THEN
        assertThat(fooNested.bar, notNullValue())
        assertThat(fooNested.bar, isA(KBar::class.java))
        assertThat(innerClass1.bar, notNullValue())
        assertThat(innerClass1.bar, isA(KBar::class.java))
        assertThat(innerClass2.bar, notNullValue())
        assertThat(innerClass2.bar, isA(KBar::class.java))
    }

    @Test
    fun testInjection_shouldFail_whenFieldsAreMasked() {
        // GIVEN
        val scope = ScopeImpl("")

        // WHEN
        val fooChildMaskingMember = scope.getInstance(KFooChildMaskingMember::class.java)
        val parentBarToString = fooChildMaskingMember.toString()

        // THEN
        assertThat(parentBarToString, notNullValue())
        assertThat(fooChildMaskingMember.bar, not<KBar>(sameInstance<KBar>(fooChildMaskingMember.superBar())))
    }

    @Test
    @Throws(Exception::class)
    fun testInjection_shouldWork_whenInheritingBinding() {
        // GIVEN

        val scope = Toothpick.openScope("root")
        scope.installModules(object : Module() {
            init {
                bind(KBar::class.java).to(KBar::class.java)
            }
        })
        val childScope = Toothpick.openScopes("root", "child")
        val foo = KFoo()

        // WHEN
        Toothpick.inject(foo, childScope)

        // THEN
        assertThat(foo.bar, notNullValue())
        assertThat(foo.bar, isA(KBar::class.java))
    }

    @Test
    @Throws(Exception::class)
    fun testInjection_shouldNotThrowAnException_whenNoDependencyIsFound() {
        // GIVEN
        val scope = ScopeImpl("root")
        val notInjectable = KNotInjectable()

        // WHEN
        Toothpick.inject(notInjectable, scope)

        // THEN
        // nothing
    }

    internal inner class KNotInjectable
}
