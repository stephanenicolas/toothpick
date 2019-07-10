package toothpick.kotlin.delegate

import com.nhaarman.mockitokotlin2.mock
import org.amshove.kluent.When
import org.amshove.kluent.calling
import org.amshove.kluent.invoking
import org.amshove.kluent.itReturns
import org.amshove.kluent.shouldBe
import org.amshove.kluent.shouldBeFalse
import org.amshove.kluent.shouldBeTrue
import org.amshove.kluent.shouldNotThrow
import org.amshove.kluent.shouldThrow
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import toothpick.Lazy
import toothpick.Scope
import javax.inject.Provider

class InjectDelegateTest {

    @Nested
    inner class EagerDelegateTest {

        @Test
        fun `should call getInstance without name on scope when entry point is injected`() {
            // GIVEN
            val delegate = EagerDelegate(MyClass::class.java, null)
            val instance = MyClass()
            val scope: Scope = mock()

            When calling scope.getInstance(MyClass::class.java, null) itReturns instance

            // WHEN
            delegate.onEntryPointInjected(scope)

            // THEN
            delegate.instance shouldBe instance
        }

        @Test
        fun `should call getInstance with name on scope when entry point is injected`() {
            // GIVEN
            val delegate = EagerDelegate(MyClass::class.java, "name")
            val instance = MyClass()
            val scope: Scope = mock()

            When calling scope.getInstance(MyClass::class.java, "name") itReturns instance

            // WHEN
            delegate.onEntryPointInjected(scope)

            // THEN
            delegate.instance shouldBe instance
        }

        @Test
        fun `isEntryPointInjected should return true when entry point is injected`() {
            // GIVEN
            val delegate = EagerDelegate(MyClass::class.java, "name")
            val scope: Scope = mock()

            When calling scope.getInstance(MyClass::class.java, "name") itReturns MyClass()

            // WHEN
            delegate.onEntryPointInjected(scope)

            // THEN
            delegate.isEntryPointInjected().shouldBeTrue()
            invoking { delegate.getValue(this, mock()) } shouldNotThrow IllegalStateException::class
        }

        @Test
        fun `isEntryPointInjected should return false when entry point is not injected`() {
            // GIVEN
            val delegate = EagerDelegate(MyClass::class.java, "name")

            // WHEN
            // nothing happens

            // THEN
            delegate.isEntryPointInjected().shouldBeFalse()
            invoking { delegate.getValue(this, mock()) } shouldThrow IllegalStateException::class
        }
    }

    @Nested
    inner class ProviderDelegateTest {

        @Test
        fun `should call getLazy without name on scope when lazy is true and entry point is injected`() {
            // GIVEN
            val delegate = ProviderDelegate(MyClass::class.java, null, true)
            val lazy: Lazy<MyClass> = mock()
            val instance = MyClass()
            val scope: Scope = mock()

            When calling scope.getLazy(MyClass::class.java, null) itReturns lazy
            When calling lazy.get() itReturns instance

            // WHEN
            delegate.onEntryPointInjected(scope)

            // THEN
            delegate.instance shouldBe instance
        }

        @Test
        fun `should call getLazy with name on scope when lazy is true and entry point is injected`() {
            // GIVEN
            val delegate = ProviderDelegate(MyClass::class.java, "name", true)
            val lazy: Lazy<MyClass> = mock()
            val instance = MyClass()
            val scope: Scope = mock()

            When calling scope.getLazy(MyClass::class.java, "name") itReturns lazy
            When calling lazy.get() itReturns instance

            // WHEN
            delegate.onEntryPointInjected(scope)

            // THEN
            delegate.instance shouldBe instance
        }

        @Test
        fun `should call getProvider without name on scope when lazy is false and entry point is injected`() {
            // GIVEN
            val delegate = ProviderDelegate(MyClass::class.java, null, false)
            val provider: Provider<MyClass> = mock()
            val instance = MyClass()
            val scope: Scope = mock()

            When calling scope.getProvider(MyClass::class.java, null) itReturns provider
            When calling provider.get() itReturns instance

            // WHEN
            delegate.onEntryPointInjected(scope)

            // THEN
            delegate.instance shouldBe instance
        }

        @Test
        fun `should call getProvider with name on scope when lazy is false and entry point is injected`() {
            // GIVEN
            val delegate = ProviderDelegate(MyClass::class.java, "name", false)
            val provider: Provider<MyClass> = mock()
            val instance = MyClass()
            val scope: Scope = mock()

            When calling scope.getProvider(MyClass::class.java, "name") itReturns provider
            When calling provider.get() itReturns instance

            // WHEN
            delegate.onEntryPointInjected(scope)

            // THEN
            delegate.instance shouldBe instance
        }

        @Test
        fun `isEntryPointInjected should return true when entry point is injected`() {
            // GIVEN
            val delegate = ProviderDelegate(MyClass::class.java, "name", false)
            val provider: Provider<MyClass> = mock()
            val scope: Scope = mock()

            When calling scope.getProvider(MyClass::class.java, "name") itReturns provider
            When calling provider.get() itReturns MyClass()

            // WHEN
            delegate.onEntryPointInjected(scope)

            // THEN
            delegate.isEntryPointInjected().shouldBeTrue()
            invoking { delegate.getValue(this, mock()) } shouldNotThrow IllegalStateException::class
        }

        @Test
        fun `isEntryPointInjected should return false when entry point is not injected`() {
            // GIVEN
            val delegate = EagerDelegate(MyClass::class.java, "name")

            // WHEN
            // nothing happens

            // THEN
            delegate.isEntryPointInjected().shouldBeFalse()
            invoking { delegate.getValue(this, mock()) } shouldThrow IllegalStateException::class
        }
    }

    class MyClass
}