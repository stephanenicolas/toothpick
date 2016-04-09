package toothpick.data;

import javax.inject.Inject;

public class FooChildWithInjectedFields extends Foo {
  @Inject public Bar bar2; //annotation is not needed, but it's a better example

  public FooChildWithInjectedFields() {
  }
}
