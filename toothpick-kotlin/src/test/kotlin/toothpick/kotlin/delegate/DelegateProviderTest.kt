package toothpick.kotlin.delegate

import com.nhaarman.mockitokotlin2.mock
import org.amshove.kluent.shouldBeInstanceOf
import org.junit.jupiter.api.Test

class DelegateProviderTest {

    @Test
    fun `provideDelegate should provide an EagerDelegate for the type EAGER`() {
        // GIVEN
        val delegateProvider = DelegateProvider(MyClass::class.java, "name", InjectionType.EAGER)

        // WHEN
        val delegate = delegateProvider.provideDelegate(this, mock())

        // THEN
        delegate shouldBeInstanceOf EagerDelegate::class
    }

    @Test
    fun `provideDelegate should provide an ProviderDelegate for the type LAZY`() {
        // GIVEN
        val delegateProvider = DelegateProvider(MyClass::class.java, "name", InjectionType.LAZY)

        // WHEN
        val delegate = delegateProvider.provideDelegate(this, mock())

        // THEN
        delegate shouldBeInstanceOf ProviderDelegate::class
    }

    @Test
    fun `provideDelegate should provide an ProviderDelegate for the type PROVIDER`() {
        // GIVEN
        val delegateProvider = DelegateProvider(MyClass::class.java, "name", InjectionType.PROVIDER)

        // WHEN
        val delegate = delegateProvider.provideDelegate(this, mock())

        // THEN
        delegate shouldBeInstanceOf ProviderDelegate::class
    }

    class MyClass
}