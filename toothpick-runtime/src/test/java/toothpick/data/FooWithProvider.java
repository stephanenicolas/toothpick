package toothpick.data;

import javax.inject.Inject;
import javax.inject.Provider;

public class FooWithProvider implements IFoo {
  @Inject public Provider<Bar> bar; //annotation is not needed, but it's a better example

  public FooWithProvider() {
  }
}
