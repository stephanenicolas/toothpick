package toothpick.integration.data;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

@Singleton public class IFooProviderAnnotatedSingleton implements Provider<IFoo> {
  public @Inject Bar bar;

  @Inject public IFooProviderAnnotatedSingleton() {
  }

  @Override public IFoo get() {
    return new Foo();
  }
}
