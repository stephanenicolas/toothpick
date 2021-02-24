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
package toothpick.ktp.binding

import javax.inject.Provider
import javax.inject.Qualifier
import org.junit.jupiter.api.Test

class BindingExtensionTest {

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
            bind<CharSequence>().withName(QualifierName::class).toInstance { "" }

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
