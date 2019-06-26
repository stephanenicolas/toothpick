package toothpick.data

import javax.inject.Inject

class KFooNested : IFoo {
    @Inject lateinit
    var bar: KBar

    class InnerClass1 {
        @Inject lateinit
        var bar: KBar

        class InnerClass2 {
            @Inject lateinit
            var bar: KBar
        }
    }
}
