package toothpick.kotlin

import javax.inject.Inject
import org.easymock.EasyMockRule
import org.easymock.Mock
import org.junit.After
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.junit.rules.TestRule
import toothpick.Toothpick

import org.easymock.EasyMock.expect
import org.easymock.EasyMock.replay
import org.easymock.EasyMock.verify
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.notNullValue
import org.junit.Assert.assertThat
import toothpick.Scope
import toothpick.testing.ToothPickRule

class TestMocking {
    @Rule @JvmField
    var toothPickRule = ToothPickRule(this, "Foo")
    @Rule @JvmField
    var chain: TestRule = RuleChain.outerRule(toothPickRule).around(EasyMockRule(this))

    @Mock lateinit var dependency: Dependency

    @After
    @Throws(Exception::class)
    fun tearDown() {
        //needs to be performed after test execution
        //not before as rule are initialized before @Before
        Toothpick.reset()
    }

    @Test
    @Throws(Exception::class)
    fun testInjectDependenciesByFieldsInNonEntryPoints() {
        //GIVEN
        expect(dependency.num()).andReturn(2)
        replay(dependency)
        //WHEN
        val nonEntryPoint = toothPickRule.getInstance(NonEntryPointByFields::class.java)
        val num = nonEntryPoint.dependency.num()
        //THEN
        verify(dependency)
        assertThat(nonEntryPoint, notNullValue())
        assertThat<Dependency>(nonEntryPoint.dependency, notNullValue())
        assertThat(num, `is`(2))
    }

    @Test
    @Throws(Exception::class)
    fun testInjectDependenciesByConstructorInNonEntryPoints() {
        //GIVEN
        expect(dependency.num()).andReturn(2)
        replay(dependency)
        //WHEN
        val nonEntryPoint = toothPickRule.getInstance(NonEntryPointByConstructor::class.java)
        val num = nonEntryPoint.dependency.num()
        //THEN
        verify(dependency)
        assertThat(nonEntryPoint, notNullValue())
        assertThat<Dependency>(nonEntryPoint.dependency, notNullValue())
        assertThat(num, `is`(2))
    }

    @Test
    @Throws(Exception::class)
    fun testInjectDependenciesByFieldsInEntryPointByMemberInjector() {
        //GIVEN
        expect(dependency.num()).andReturn(2)
        replay(dependency)
        //WHEN
        val nonEntryPoint = EntryPointByMemberInjector()
        val num = nonEntryPoint.dependency.num()
        //THEN
        verify(dependency)
        assertThat(nonEntryPoint, notNullValue())
        assertThat<Dependency>(nonEntryPoint.dependency, notNullValue())
        assertThat(num, `is`(2))
    }

    @Test
    @Throws(Exception::class)
    fun testInjectDependenciesByFieldsInEntryPointByDelegate() {
        //GIVEN
        expect(dependency.num()).andReturn(2)
        replay(dependency)
        //WHEN
        val nonEntryPoint = EntryPointByDelegate()
        val num = nonEntryPoint.dependency.num()
        //THEN
        verify(dependency)
        assertThat(nonEntryPoint, notNullValue())
        assertThat<Dependency>(nonEntryPoint.dependency, notNullValue())
        assertThat(num, `is`(2))
    }

    class EntryPointByMemberInjector {
        @Inject lateinit var dependency: Dependency

        constructor() {
            Toothpick.inject(this, Toothpick.openScope("Foo"))
        }
    }

    class EntryPointByDelegate {
        val dependency: Dependency by scope.inject()

        val scope: Scope
        get() = Toothpick.openScope("Foo")
    }

    class NonEntryPointByFields @Inject constructor(scope: Scope) {
        val dependency: Dependency by KTP.inject()
    }

    class NonEntryPointByConstructor @Inject constructor(val dependency: Dependency)

   //open for mocking
   open class Dependency {
        open fun num(): Int {
            return 1
        }
    }
}
