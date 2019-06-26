package toothpick.data;

import javax.inject.Inject;

class KFoo @Inject constructor() : IFoo {
  @Inject lateinit var bar: KBar  //annotation is not needed, but it's a better example
}
