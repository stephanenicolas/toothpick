package toothpick.data;

import javax.inject.Inject;
import javax.inject.Provider;
import toothpick.ProvidesSingletonInScope;

@ProvidesSingletonInScope
@CustomScope
public class FooProviderAnnotatedProvidesSingleton implements Provider<IFoo> {
  @Inject
  public FooProviderAnnotatedProvidesSingleton() {
  }

  @Override
  public IFoo get() {
    return new Foo();
  }
}
