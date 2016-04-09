package toothpick.data;

import javax.inject.Inject;

public class Foo implements IFoo {
  @Inject public Bar bar; //annotation is not needed, but it's a better example

  @Inject public Foo() {
  }
}
