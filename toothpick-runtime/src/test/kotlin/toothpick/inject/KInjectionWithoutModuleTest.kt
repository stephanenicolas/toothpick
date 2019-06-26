package toothpick.inject

import org.hamcrest.CoreMatchers.*
import org.junit.Test
import toothpick.ScopeImpl
import toothpick.Toothpick

import org.hamcrest.MatcherAssert.assertThat
import toothpick.config.Module
import toothpick.data.*

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

    @Test
    fun testNestedClassInjection() {
        //GIVEN
        val scope = ScopeImpl("")

        //WHEN
        val fooNested = scope.getInstance(KFooNested::class.java)
        val innerClass1 = scope.getInstance(KFooNested.InnerClass1::class.java)
        val innerClass2 = scope.getInstance(KFooNested.InnerClass1.InnerClass2::class.java)

        //THEN
        assertThat(fooNested.bar, notNullValue())
        assertThat(fooNested.bar, isA(KBar::class.java))
        assertThat(innerClass1.bar, notNullValue())
        assertThat(innerClass1.bar, isA(KBar::class.java))
        assertThat(innerClass2.bar, notNullValue())
        assertThat(innerClass2.bar, isA(KBar::class.java))
    }

    @Test
    fun testInjection_shouldFail_whenFieldsAreMasked() {
        //GIVEN
        val scope = ScopeImpl("")

        //WHEN
        val fooChildMaskingMember = scope.getInstance(KFooChildMaskingMember::class.java)
        val parentBarToString = fooChildMaskingMember.toString()

        //THEN
        assertThat(parentBarToString, notNullValue())
        assertThat(fooChildMaskingMember.bar, not<KBar>(sameInstance<KBar>(fooChildMaskingMember.superBar())))
    }

    @Test
    @Throws(Exception::class)
    fun testInjection_shouldWork_whenInheritingBinding() {
        //GIVEN

        val scope = Toothpick.openScope("root")
        scope.installModules(object : Module() {
            init {
                bind(KBar::class.java).to(KBar::class.java)
            }
        })
        val childScope = Toothpick.openScopes("root", "child")
        val foo = KFoo()

        //WHEN
        Toothpick.inject(foo, childScope)

        //THEN
        assertThat(foo.bar, notNullValue())
        assertThat(foo.bar, isA(KBar::class.java))
    }

    @Test
    @Throws(Exception::class)
    fun testInjection_shouldNotThrowAnException_whenNoDependencyIsFound() {
        //GIVEN
        val scope = ScopeImpl("root")
        val notInjectable = KNotInjectable()

        //WHEN
        Toothpick.inject(notInjectable, scope)

        //THEN
        // nothing
    }

    internal inner class KNotInjectable
}
