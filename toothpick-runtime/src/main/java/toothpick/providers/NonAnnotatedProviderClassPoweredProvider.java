package toothpick.providers;

import toothpick.Factory;
import toothpick.FactoryRegistry;
import toothpick.InjectorImpl;
import toothpick.Provider;

public class NonAnnotatedProviderClassPoweredProvider<T> extends BaseProvider<T> {
  private final Class<T> key;
  private final Class<? extends Provider<T>> providerClass;

  public NonAnnotatedProviderClassPoweredProvider(InjectorImpl injector, Class<T> key,
      Class<? extends Provider<T>> providerClass) {
    super(injector);
    this.key = key;
    this.providerClass = providerClass;
  }

  @Override public T get() {
    Factory<? extends Provider<T>> providerFactory = FactoryRegistry.getFactory(providerClass);
    getInjector().getScope().put(key, new ProducerFactoryPoweredProvider(getInjector(), providerFactory));
    return providerFactory.createInstance(getInjector()).get();
  }
}
