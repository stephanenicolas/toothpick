package toothpick.data;

import javax.inject.Inject;

public class FooChildMaskingMember extends FooParentMaskingMember {
  @Inject public Bar bar; //annotation is not needed, but it's a better example

  @Inject
  public FooChildMaskingMember() {
  }
}
