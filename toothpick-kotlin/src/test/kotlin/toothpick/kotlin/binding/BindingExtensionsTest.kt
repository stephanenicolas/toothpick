package toothpick.kotlin.binding

import org.junit.jupiter.api.Test


class BindingExtensionsTest {

    @Test
    fun testBindingApi() {
        module {
            bind(String::class)
            bind<String>()
            bind<String>().singleton()
            bind<String>().singleton().releasable()

            bind(CharSequence::class).toClass(String::class)
            bind<CharSequence>().toClass<String>()
            bind<CharSequence>().withName("").toClass<String>()
            bind<CharSequence>().toClass<String>().singleton()
            bind<CharSequence>().toClass<String>().singleton().releasable()

            bind<CharSequence>().toInstance("")
            bind<CharSequence>().withName("").toInstance("")
            bind<CharSequence>().toInstance { "" }
            bind<CharSequence>().withName("").toInstance { "" }
        }
    }
}
