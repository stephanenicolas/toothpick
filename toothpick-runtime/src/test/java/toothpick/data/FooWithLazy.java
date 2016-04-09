package toothpick.data;

import javax.inject.Inject;
import toothpick.Lazy;

public class FooWithLazy implements IFoo {
  @Inject public Lazy<Bar> bar; //annotation is not needed, but it's a better example

  public FooWithLazy() {
  }
}
