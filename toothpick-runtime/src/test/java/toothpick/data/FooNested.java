package toothpick.data;

import javax.inject.Inject;

public class FooNested implements IFoo {
  @Inject public Bar bar;

  public static class InnerClass1 {
    @Inject public Bar bar;

    public static class InnerClass2 {
      @Inject public Bar bar;
    }
  }
}
