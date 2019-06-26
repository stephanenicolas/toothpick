package toothpick.data

import javax.inject.Inject
import javax.inject.Named
import toothpick.Lazy

class KFooWithNamedLazy : IFoo {
    @Inject
    @field:Named("foo")
    lateinit var bar: Lazy<KBar> //annotation is not needed, but it's a better example
}