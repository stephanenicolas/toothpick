package toothpick.data;

import javax.inject.Inject;
import javax.inject.Provider;

public class FooWithProviderOfSingleton implements IFoo {
  @Inject public Provider<FooSingleton> fooSingletonProvider; //annotation is not needed, but it's a better example

  public FooWithProviderOfSingleton() {
  }
}
