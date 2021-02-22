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
package toothpick.ktp

import javax.inject.Named
import javax.inject.Provider
import javax.inject.Qualifier
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
import toothpick.ktp.delegate.inject
import toothpick.ktp.delegate.lazy
import toothpick.ktp.delegate.provider
import toothpick.ktp.extension.getInstance
import toothpick.ktp.extension.getLazy
import toothpick.ktp.extension.getProvider
import toothpick.testing.ToothPickExtension

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
        When calling qualifierDependency.num() itReturns 4

        // WHEN
        val entryPoint = EntryPoint()

        // THEN

        assertDependencies(entryPoint, 2, 3, 4)
    }

    @Test
    fun `constructor injection by getInstance should inject dependencies when they are defined`() {
        // GIVEN
        When calling dependency.num() itReturns 2
        When calling namedDependency.num() itReturns 3
        When calling qualifierDependency.num() itReturns 4

        // WHEN
        val nonEntryPoint: NonEntryPoint = KTP.openScope("Foo").getInstance()

        // THEN
        assertDependencies(nonEntryPoint, 2, 3, 4)
    }

    @Test
    fun `constructor injection byLazy should inject dependencies when they are defined`() {
        // GIVEN
        When calling dependency.num() itReturns 2
        When calling namedDependency.num() itReturns 3
        When calling qualifierDependency.num() itReturns 4

        // WHEN
        val nonEntryPoint: Lazy<NonEntryPoint> = KTP.openScope("Foo").getLazy()

        // THEN
        assertDependencies(nonEntryPoint.get(), 2, 3, 4)
    }

    @Test
    fun `constructor injection by provider should inject dependencies when they are defined`() {
        // GIVEN
        When calling dependency.num() itReturns 2
        When calling namedDependency.num() itReturns 3
        When calling qualifierDependency.num() itReturns 4

        // WHEN
        val nonEntryPoint: Provider<NonEntryPoint> = KTP.openScope("Foo").getProvider()

        // THEN
        assertDependencies(nonEntryPoint.get(), 2, 3, 4)
    }

    private fun assertDependencies(
      nonEntryPoint: NonEntryPoint,
      dependencyValue: Int,
      namedDependencyValue: Int,
      qualifierDependencyValue: Int
    ) {
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

        nonEntryPoint.dependency.num() shouldEqual dependencyValue
        nonEntryPoint.lazyDependency.get().num() shouldEqual dependencyValue
        nonEntryPoint.providerDependency.get().num() shouldEqual dependencyValue
        nonEntryPoint.namedDependency.num() shouldEqual namedDependencyValue
        nonEntryPoint.namedLazyDependency.get().num() shouldEqual namedDependencyValue
        nonEntryPoint.namedProviderDependency.get().num() shouldEqual namedDependencyValue
        nonEntryPoint.qualifierDependency.num() shouldEqual qualifierDependencyValue
        nonEntryPoint.qualifierLazyDependency.get().num() shouldEqual qualifierDependencyValue
        nonEntryPoint.qualifierProviderDependency.get().num() shouldEqual qualifierDependencyValue
    }

    private fun assertDependencies(
      entryPoint: EntryPoint,
      dependencyValue: Int,
      namedDependencyValue: Int,
      qualifierDependencyValue: Int
    ) {
        entryPoint.shouldNotBeNull()
        entryPoint.dependency.shouldNotBeNull()
        entryPoint.lazyDependency.shouldNotBeNull()
        entryPoint.providerDependency.shouldNotBeNull()
        entryPoint.namedDependency.shouldNotBeNull()
        entryPoint.namedLazyDependency.shouldNotBeNull()
        entryPoint.namedProviderDependency.shouldNotBeNull()
        entryPoint.qualifierDependency.shouldNotBeNull()
        entryPoint.qualifierLazyDependency.shouldNotBeNull()
        entryPoint.qualifierProviderDependency.shouldNotBeNull()

        entryPoint.dependency.num() shouldEqual dependencyValue
        entryPoint.lazyDependency.num() shouldEqual dependencyValue
        entryPoint.providerDependency.num() shouldEqual dependencyValue
        entryPoint.namedDependency.num() shouldEqual namedDependencyValue
        entryPoint.namedLazyDependency.num() shouldEqual namedDependencyValue
        entryPoint.namedProviderDependency.num() shouldEqual namedDependencyValue
        entryPoint.qualifierDependency.num() shouldEqual qualifierDependencyValue
        entryPoint.qualifierLazyDependency.num() shouldEqual qualifierDependencyValue
    }

    class EntryPoint {
        val dependency: Dependency by inject()
        val lazyDependency: Dependency by lazy()
        val providerDependency: Dependency by provider()
        val namedDependency: Dependency by inject("name")
        val namedLazyDependency: Dependency by lazy("name")
        val namedProviderDependency: Dependency by provider("name")
        val qualifierDependency: Dependency by inject(QualifierName::class)
        val qualifierLazyDependency: Dependency by lazy(QualifierName::class)
        val qualifierProviderDependency: Dependency by provider(QualifierName::class)

        init {
            KTP.openScope("Foo").inject(this)
        }
    }

    @InjectConstructor
    class NonEntryPoint(
      val dependency: Dependency,
      val lazyDependency: Lazy<Dependency>,
      val providerDependency: Provider<Dependency>,
      @Named("name") val namedDependency: Dependency,
      @Named("name") val namedLazyDependency: Lazy<Dependency>,
      @Named("name") val namedProviderDependency: Provider<Dependency>,
      @QualifierName val qualifierDependency: Dependency,
      @QualifierName val qualifierLazyDependency: Lazy<Dependency>,
      @QualifierName val qualifierProviderDependency: Provider<Dependency>
    )

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
