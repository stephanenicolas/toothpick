package toothpick.data;

import javax.inject.Inject;
import javax.inject.Provider;
import toothpick.ScopeInstances;

@ScopeInstances
@CustomScope
public class IFooProviderAnnotatedProvidesSingleton implements Provider<IFoo> {
  @Inject Bar bar;

  @Inject
  public IFooProviderAnnotatedProvidesSingleton() {
  }

  @Override
  public IFoo get() {
    return new Foo();
  }
}
