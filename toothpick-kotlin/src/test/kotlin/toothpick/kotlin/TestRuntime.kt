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
import toothpick.InjectConstructor
import toothpick.Lazy
import toothpick.kotlin.delegate.inject
import toothpick.kotlin.delegate.lazy
import toothpick.kotlin.delegate.provider
import toothpick.testing.ToothPickExtension
import javax.inject.Named
import javax.inject.Provider
import javax.inject.Qualifier

/*
+ test cases for binding
+ extend new methods for scope (lifecycle, and new methods)
+ add javadoc to everything
 */
class TestRuntime {

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
    @Mock
    @field:QualifierName
    lateinit var qualifierDependency: Dependency

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
    fun `constructor injection should inject mocks when they are defined`() {
        // GIVEN
        When calling dependency.num() itReturns 2
        When calling namedDependency.num() itReturns 3
        When calling qualifierDependency.num() itReturns 4

        // WHEN
        val nonEntryPoint: NonEntryPoint = KTP.openScope("Foo").getInstance()

        // THEN
        nonEntryPoint.shouldNotBeNull()
        nonEntryPoint.dependency.shouldNotBeNull()
        nonEntryPoint.lazyDependency.shouldNotBeNull()
        nonEntryPoint.providerDependency.shouldNotBeNull()
        nonEntryPoint.namedDependency.shouldNotBeNull()
        nonEntryPoint.namedLazyDependency.shouldNotBeNull()
        nonEntryPoint.namedProviderDependency.shouldNotBeNull()
        nonEntryPoint.qualifierDependency.shouldNotBeNull()
        nonEntryPoint.qualifierLazyDependency.shouldNotBeNull()
        nonEntryPoint.qualifierProviderDependency.shouldNotBeNull()

        nonEntryPoint.dependency.num() shouldEqual 2
        nonEntryPoint.lazyDependency.get().num() shouldEqual 2
        nonEntryPoint.providerDependency.get().num() shouldEqual 2
        nonEntryPoint.namedDependency.num() shouldEqual 3
        nonEntryPoint.namedLazyDependency.get().num() shouldEqual 3
        nonEntryPoint.namedProviderDependency.get().num() shouldEqual 3
        nonEntryPoint.qualifierDependency.num() shouldEqual 4
        nonEntryPoint.qualifierLazyDependency.get().num() shouldEqual 4
        nonEntryPoint.qualifierProviderDependency.get().num() shouldEqual 4
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

    @InjectConstructor
    class NonEntryPoint(val dependency: Dependency,
                        val lazyDependency: Lazy<Dependency>,
                        val providerDependency: Provider<Dependency>,
                        @Named("name") val namedDependency: Dependency,
                        @Named("name") val namedLazyDependency: Lazy<Dependency>,
                        @Named("name") val namedProviderDependency: Provider<Dependency>,
                        @QualifierName val qualifierDependency: Dependency,
                        @QualifierName val qualifierLazyDependency: Lazy<Dependency>,
                        @QualifierName val qualifierProviderDependency: Provider<Dependency>)

    // open for mocking
    open class Dependency {
        open fun num(): Int {
            return 1
        }
    }

    @Qualifier
    @Retention(AnnotationRetention.RUNTIME)
    annotation class QualifierName
}
