package toothpick.integration.data;

import javax.inject.Inject;
import javax.inject.Singleton;
import toothpick.Provider;

@Singleton public class IFooProviderAnnotatedSingleton implements Provider<IFoo> {
  @Inject Bar bar;

  @Inject public IFooProviderAnnotatedSingleton() {
  }

  @Override public IFoo get() {
    return new Foo();
  }
}
