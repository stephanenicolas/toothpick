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
package toothpick.kotlin

import org.amshove.kluent.Verify
import org.amshove.kluent.When
import org.amshove.kluent.called
import org.amshove.kluent.calling
import org.amshove.kluent.itReturns
import org.amshove.kluent.on
import org.amshove.kluent.shouldEqual
import org.amshove.kluent.shouldNotBeNull
import org.amshove.kluent.that
import org.amshove.kluent.was
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import toothpick.kotlin.delegate.inject
import toothpick.testing.ToothPickExtension
import javax.inject.Inject

/*
+ Lazy <<<
+ Provider <<<
+ Named <<<
+ Concurrency <<<
* qualifiers only for constructor <<<
+ Tests ^

+ Compiler -> @InjectAnnotation for class ???
+ Module and bindings (Danny Preussler or Cody) https://github.com/sporttotal-tv/toothpick-kotlin-extensions   &    https://github.com/stephanenicolas/toothpick/issues/305
 */
class TestMocking {

    @JvmField
    @RegisterExtension
    val toothpickExtension = ToothPickExtension(this, "Foo")
    @JvmField
    @RegisterExtension
    val mockitoExtension = MockitoExtension()

    @Mock
    lateinit var dependency: Dependency

    @Test
    fun testInjectDependenciesByFieldsInEntryPointByMemberInjector() {
        // GIVEN
        When calling dependency.num() itReturns 2

        // WHEN
        val nonEntryPoint = EntryPoint()
        val num = nonEntryPoint.dependency.num()

        // THEN
        Verify on dependency that dependency.num() was called
        nonEntryPoint.shouldNotBeNull()
        nonEntryPoint.dependency.shouldNotBeNull()
        num shouldEqual 2
    }

    @Test
    fun testInjectDependenciesByConstructorInNonEntryPoints() {
        // GIVEN
        When calling dependency.num() itReturns 2

        // WHEN
        val nonEntryPoint = KTP.openScope("Foo").getInstance(NonEntryPointByConstructor::class.java)
        val num = nonEntryPoint.dependency.num()

        // THEN
        Verify on dependency that dependency.num() was called
        nonEntryPoint.shouldNotBeNull()
        nonEntryPoint.dependency.shouldNotBeNull()
        num shouldEqual 2
    }

    class EntryPoint {
        val dependency: Dependency by inject()

        init {
            KTP.openScope("Foo").inject(this)
        }
    }

    class NonEntryPointByConstructor @Inject constructor(val dependency: Dependency)

    // open for mocking
    open class Dependency {
        open fun num(): Int {
            return 1
        }
    }
}
