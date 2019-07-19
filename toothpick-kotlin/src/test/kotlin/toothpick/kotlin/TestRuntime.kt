package toothpick.kotlin

import toothpick.kotlin.delegate.inject
import javax.inject.Inject

class TestRuntime {

    class EntryPoint {
        val dependency: Dependency by inject()

        init {
            KTP.openScope("Foo").inject(this)
        }
    }

    class NonEntryPoint @Inject constructor(val dependency: Dependency)

    class Dependency
}
