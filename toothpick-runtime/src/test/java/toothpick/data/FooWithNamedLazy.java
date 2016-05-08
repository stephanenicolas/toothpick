package toothpick.data;

import javax.inject.Inject;
import javax.inject.Named;
import toothpick.Lazy;

public class FooWithNamedLazy implements IFoo {
  @Inject @Named("foo") public Lazy<Bar> bar; //annotation is not needed, but it's a better example

  public FooWithNamedLazy() {
  }
}
