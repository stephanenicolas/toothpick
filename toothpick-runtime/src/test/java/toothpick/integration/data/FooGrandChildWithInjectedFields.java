package toothpick.integration.data;

import javax.inject.Inject;

public class FooGrandChildWithInjectedFields extends Foo {
  public @Inject Bar bar2;

  public FooGrandChildWithInjectedFields() {
  }
}
