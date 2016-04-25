package toothpick.data;

import javax.inject.Inject;
import javax.inject.Provider;
import toothpick.ScopeInstances;
import toothpick.Scoped;

@ScopeInstances
@Scoped
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
