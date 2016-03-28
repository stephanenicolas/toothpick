package toothpick.providers;

import toothpick.Factory;
import toothpick.FactoryRegistry;
import toothpick.InjectorImpl;
import toothpick.Provider;

public class SingletonAnnotatedProviderClassPoweredProvider<T> extends ReplaceInScopeProvider<T> {
  private Class<? extends Provider<? extends T>> providerClass;

  public SingletonAnnotatedProviderClassPoweredProvider(InjectorImpl injector, Class<T> key,
      Class<? extends Provider<? extends T>> providerClass) {
    super(injector, key);
    this.providerClass = providerClass;
  }

  @Override
  public T get() {
    Factory<? extends Provider<? extends T>> providerFactory =
        FactoryRegistry.getFactory(providerClass);
    Provider<? extends T> provider = providerFactory.createInstance(getInjector());
    replaceInScope(provider);
    return null;
  }
}
