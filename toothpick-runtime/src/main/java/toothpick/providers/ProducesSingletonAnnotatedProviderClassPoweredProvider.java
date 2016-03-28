package toothpick.providers;

import toothpick.Factory;
import toothpick.FactoryRegistry;
import toothpick.InjectorImpl;
import toothpick.Provider;

public class ProducesSingletonAnnotatedProviderClassPoweredProvider<T> extends BaseProvider<T> {
  private Class<T> key;
  private Class<? extends Provider<? extends T>> providerClass;

  public ProducesSingletonAnnotatedProviderClassPoweredProvider(InjectorImpl injector, Class<T> key,
      Class<? extends Provider<? extends T>> providerClass) {
    super(injector);
    this.key = key;
    this.providerClass = providerClass;
  }

  @Override public T get() {
    Factory<? extends Provider<? extends T>> providerFactory =
        FactoryRegistry.getFactory(providerClass);
    Provider<? extends T> provider = providerFactory.createInstance(getInjector());
    T instance = provider.get();
    getInjector().getScope().put(key, new SingletonPoweredProvider<>(instance));
    return null;
  }
}
