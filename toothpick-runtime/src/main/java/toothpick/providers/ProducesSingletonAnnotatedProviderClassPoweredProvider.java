package toothpick.providers;

import toothpick.Factory;
import toothpick.FactoryRegistry;
import toothpick.InjectorImpl;
import toothpick.Provider;

public class ProducesSingletonAnnotatedProviderClassPoweredProvider<T> extends ReplaceInScopeProvider<T> {
  private Class<? extends Provider<? extends T>> providerClass;

  public ProducesSingletonAnnotatedProviderClassPoweredProvider(InjectorImpl injector, Class<T> key,
      Class<? extends Provider<? extends T>> providerClass) {
    super(injector, key);
    this.providerClass = providerClass;
  }

  @Override public T get() {
    Factory<? extends Provider<? extends T>> providerFactory =
        FactoryRegistry.getFactory(providerClass);
    Provider<? extends T> provider = providerFactory.createInstance(getInjector());
    T instance = provider.get();
    replaceInScope(new SingletonPoweredProvider<>(instance));
    return null;
  }
}
