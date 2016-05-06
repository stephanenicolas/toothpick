package toothpick.data;

import javax.inject.Inject;

public class CyclicFoo implements IFoo {
  @Inject public CyclicFoo cyclicFoo; //annotation is not needed, but it's a better example

  @Inject
  public CyclicFoo() {
  }
}
