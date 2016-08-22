package toothpick.data;

import javax.inject.Inject;
import javax.inject.Named;

public class CyclicNamedFoo implements IFoo {
  @Inject @Named("foo") public CyclicNamedFoo cyclicFoo;

  @Inject
  public CyclicNamedFoo() {
  }
}
