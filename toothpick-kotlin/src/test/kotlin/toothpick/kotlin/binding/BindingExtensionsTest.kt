package toothpick.kotlin.binding

import org.junit.jupiter.api.Test
import javax.inject.Provider
import javax.inject.Qualifier


class BindingExtensionsTest {

    @Test
    fun testBindingApi() {
        module {
            // bind
            bind(String::class)
            bind<String>() // equivalent to previous one
            bind<String>().singleton()
            bind<String>().singleton().releasable()

            // toClass
            bind(CharSequence::class).toClass(String::class)
            bind<CharSequence>().toClass<String>() // equivalent to previous one
            bind<CharSequence>().withName("").toClass<String>()
            bind<CharSequence>().withName(QualifierName::class).toClass<String>()
            bind<CharSequence>().toClass<String>().singleton()
            bind<CharSequence>().toClass<String>().singleton().releasable()

            // toInstance
            bind<CharSequence>().toInstance("")
            bind<CharSequence>().toInstance { "" } // equivalent to previous one
            bind<CharSequence>().withName("").toInstance { "" }
            bind<CharSequence>().withName(QualifierName::class).toClass<String>()

            // toProvider
            bind<CharSequence>().toProvider(StringProvider::class)
            bind<CharSequence>().withName("").toProvider(StringProvider::class)
            bind<CharSequence>().withName(QualifierName::class).toProvider(StringProvider::class)
            bind<CharSequence>().toProvider(StringProvider::class)
            bind<CharSequence>().toProvider(StringProvider::class).singleton()
            bind<CharSequence>().toProvider(StringProvider::class).singleton().releasable()
            bind<CharSequence>().toProvider(StringProvider::class).providesSingleton()
            bind<CharSequence>().toProvider(StringProvider::class).providesSingleton().providesReleasable()
            bind<CharSequence>().toProvider(StringProvider::class).providesSingleton().providesReleasable().singleton()

            // toProviderInstance
            bind<CharSequence>().toProviderInstance(StringProvider())
            bind<CharSequence>().toProviderInstance { "" } // equivalent to previous one
            bind<CharSequence>().withName("").toProviderInstance { "" }
            bind<CharSequence>().withName(QualifierName::class).toProviderInstance { "" }
            bind<CharSequence>().toProviderInstance { "" }.providesSingleton()
            bind<CharSequence>().toProviderInstance { "" }.providesSingleton().providesReleasable()
        }
    }

    private class StringProvider : Provider<String> {
        override fun get() = ""
    }

    @Qualifier
    @Retention(AnnotationRetention.RUNTIME)
    annotation class QualifierName
}
