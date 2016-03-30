package toothpick.integration.data;

import javax.inject.Inject;
import javax.inject.Provider;
import toothpick.ProvidesSingleton;

@ProvidesSingleton public class IFooProviderAnnotatedProvidesSingleton implements Provider<IFoo> {
  @Inject Bar bar;

  @Inject public IFooProviderAnnotatedProvidesSingleton() {
  }

  @Override public IFoo get() {
    return new Foo();
  }
}
