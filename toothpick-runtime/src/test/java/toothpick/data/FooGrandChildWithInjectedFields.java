package toothpick.data;

import javax.inject.Inject;

public class FooGrandChildWithInjectedFields extends FooChildWithoutInjectedFields {
  public @Inject Bar bar2;

  public FooGrandChildWithInjectedFields() {
  }
}
