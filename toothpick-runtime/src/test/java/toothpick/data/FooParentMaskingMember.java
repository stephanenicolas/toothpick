package toothpick.data;

import javax.inject.Inject;

public class FooParentMaskingMember implements IFoo {
  @Inject public Bar bar; //annotation is not needed, but it's a better example

  @Inject
  public FooParentMaskingMember() {
  }

  @Override
  public String toString() {
    return bar.toString();
  }
}
