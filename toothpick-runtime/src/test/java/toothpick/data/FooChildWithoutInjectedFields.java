package toothpick.data;

import javax.inject.Inject;

public class FooChildWithoutInjectedFields extends Foo {
  @Inject
  public FooChildWithoutInjectedFields() {
  }
}
