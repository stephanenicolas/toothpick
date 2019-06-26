package toothpick.data

import javax.inject.Inject

class KFooChildMaskingMember @Inject
constructor() : KFooParentMaskingMember() {
    @Inject override lateinit
    var bar: KBar  //annotation is not needed, but it's a better example

    fun superBar() = super.bar
}