package toothpick.kotlin

import com.example.toothpick.ktp.KTP
import javax.inject.Inject
import org.easymock.EasyMockRule
import org.easymock.Mock
import org.junit.After
import org.junit.Rule
import org.junit.Test
import toothpick.Toothpick

import org.easymock.EasyMock.expect
import org.easymock.EasyMock.replay
import org.easymock.EasyMock.verify
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.notNullValue
import org.junit.Assert.assertThat
import toothpick.Scope
import toothpick.testing.ToothPickRule

/*
+ Lazy
+ Provider
+ Named
* qualifiers only for constructor
+ Tests ^
+ Concurrency

+ Default scope
+ Compiler -> @InjectAnnotation for class ???
+ Module and bindings (Danny Preussler or Cody) https://github.com/sporttotal-tv/toothpick-kotlin-extensions   &    https://github.com/stephanenicolas/toothpick/issues/305
+ Optionals
 */
class TestMocking {
    @Rule @JvmField
    var toothPickRule = ToothPickRule(this, "Foo")
    @Rule @JvmField
    var easyMockRule = EasyMockRule(this)

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
        val nonEntryPoint = NonEntryPointByFields()
        KTP.openScope("Foo").inject(nonEntryPoint)
        val num = nonEntryPoint.dependency.num()

        //THEN
        verify(dependency)
        assertThat(nonEntryPoint, notNullValue())
        assertThat(nonEntryPoint.dependency, notNullValue())
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
        assertThat(nonEntryPoint.dependency, notNullValue())
        assertThat(num, `is`(2))
    }

    @Test
    @Throws(Exception::class)
    fun testInjectDependenciesByFieldsInEntryPointByMemberInjector() {
        //GIVEN
        expect(dependency.num()).andReturn(2)
        replay(dependency)
        //WHEN
        val nonEntryPoint = EntryPoint()
        val num = nonEntryPoint.dependency.num()
        //THEN
        verify(dependency)
        assertThat(nonEntryPoint, notNullValue())
        assertThat(nonEntryPoint.dependency, notNullValue())
        assertThat(num, `is`(2))
    }

    class EntryPoint {
        val dependency: Dependency by inject()

        init {
            KTP.openScope("Foo").inject(this)
        }
    }

    class NonEntryPointByFields {
        val dependency: Dependency by inject()
    }

    class NonEntryPointByConstructor @Inject constructor(val dependency: Dependency)

   //open for mocking
   open class Dependency {
        open fun num(): Int {
            return 1
        }
    }
}
