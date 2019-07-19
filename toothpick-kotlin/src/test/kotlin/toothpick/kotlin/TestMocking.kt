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

import org.amshove.kluent.When
import org.amshove.kluent.calling
import org.amshove.kluent.itReturns
import org.amshove.kluent.shouldEqual
import org.amshove.kluent.shouldNotBeNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import toothpick.Lazy
import toothpick.kotlin.delegate.inject
import toothpick.kotlin.delegate.lazy
import toothpick.kotlin.delegate.provider
import toothpick.testing.ToothPickExtension
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Provider

/*
+ Lazy <<<
+ Provider <<<
+ Named <<<
+ Concurrency <<<
* qualifiers only for constructor <<<
+ Tests ^
+ getinstance reified for scope
+ fix binding dsl

+ supporting mockk
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
    @Mock
    @field:Named("name")
    lateinit var namedDependency: Dependency

    @Test
    fun `field injection should inject mocks when they are defined`() {
        // GIVEN
        When calling dependency.num() itReturns 2
        When calling namedDependency.num() itReturns 3

        // WHEN
        val nonEntryPoint = EntryPoint()

        // THEN
        nonEntryPoint.shouldNotBeNull()
        nonEntryPoint.dependency.shouldNotBeNull()
        nonEntryPoint.lazyDependency.shouldNotBeNull()
        nonEntryPoint.providerDependency.shouldNotBeNull()
        nonEntryPoint.namedDependency.shouldNotBeNull()
        nonEntryPoint.namedLazyDependency.shouldNotBeNull()
        nonEntryPoint.namedProviderDependency.shouldNotBeNull()

        nonEntryPoint.dependency.num() shouldEqual 2
        nonEntryPoint.lazyDependency.num() shouldEqual 2
        nonEntryPoint.providerDependency.num() shouldEqual 2
        nonEntryPoint.namedDependency.num() shouldEqual 3
        nonEntryPoint.namedLazyDependency.num() shouldEqual 3
        nonEntryPoint.namedProviderDependency.num() shouldEqual 3
    }

    @Test
    fun testInjectDependenciesByConstructorInNonEntryPoints() {
        // GIVEN
        When calling dependency.num() itReturns 2
        When calling namedDependency.num() itReturns 3

        // WHEN
        val nonEntryPoint = KTP.openScope("Foo").getInstance(NonEntryPoint::class.java)
        val num = nonEntryPoint.dependency.num()

        // THEN
        nonEntryPoint.shouldNotBeNull()
        nonEntryPoint.dependency.shouldNotBeNull()
        nonEntryPoint.lazyDependency.shouldNotBeNull()
        nonEntryPoint.providerDependency.shouldNotBeNull()
        nonEntryPoint.namedDependency.shouldNotBeNull()
        nonEntryPoint.namedLazyDependency.shouldNotBeNull()
        nonEntryPoint.namedProviderDependency.shouldNotBeNull()

        nonEntryPoint.dependency.num() shouldEqual 2
        nonEntryPoint.lazyDependency.get().num() shouldEqual 2
        nonEntryPoint.providerDependency.get().num() shouldEqual 2
        nonEntryPoint.namedDependency.num() shouldEqual 3
        nonEntryPoint.namedLazyDependency.get().num() shouldEqual 3
        nonEntryPoint.namedProviderDependency.get().num() shouldEqual 3
    }

    class EntryPoint {
        val dependency: Dependency by inject()
        val lazyDependency: Dependency by lazy()
        val providerDependency: Dependency by provider()
        val namedDependency: Dependency by inject("name")
        val namedLazyDependency: Dependency by lazy("name")
        val namedProviderDependency: Dependency by provider("name")

        init {
            KTP.openScope("Foo").inject(this)
        }
    }

    class NonEntryPoint @Inject constructor(val dependency: Dependency,
                                            val lazyDependency: Lazy<Dependency>,
                                            val providerDependency: Provider<Dependency>,
                                            @Named("name") val namedDependency: Dependency,
                                            @Named("name") val namedLazyDependency: Lazy<Dependency>,
                                            @Named("name") val namedProviderDependency: Provider<Dependency>)

    // open for mocking
    open class Dependency {
        open fun num(): Int {
            return 1
        }
    }
}
