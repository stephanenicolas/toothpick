package toothpick.data

import javax.inject.Inject

open class KFooParentMaskingMember @Inject constructor() : IFoo {
    @Inject open lateinit
    var bar: KBar //annotation is not needed, but it's a better example

    override fun toString(): String {
        return bar.toString()
    }
}