package toothpick.data

import javax.inject.Inject
import toothpick.Lazy

class KFooWithLazy : IFoo {
    @Inject lateinit
    var bar: Lazy<KBar> //annotation is not needed, but it's a better example
}
