package toothpick.inject

import org.junit.Test
import toothpick.Scope
import toothpick.ScopeImpl
import toothpick.Toothpick
import toothpick.data.KBar
import toothpick.data.KFoo

import org.hamcrest.CoreMatchers.isA
import org.hamcrest.CoreMatchers.notNullValue
import org.hamcrest.MatcherAssert.assertThat

class KInjectionWithoutModuleTest {

    @Test
    fun testSimpleInjection() {
        //GIVEN
        val scope = ScopeImpl("")
        val foo = KFoo()

        //WHEN
        Toothpick.inject(foo, scope)

        //THEN
        assertThat(foo.bar, notNullValue())
        assertThat(foo.bar, isA(KBar::class.java))
    }
}
