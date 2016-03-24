package toothpick.providers;

import toothpick.Factory;
import toothpick.FactoryRegistry;
import toothpick.Provider;

/**
 * Created by snicolas on 3/24/16.
 */
public class NonAnnotatedProviderClassPoweredProvider<T> extends BaseProvider<T> {
  private final Class<T> key;
  private final Class<? extends Provider<T>> providerClass;

  public NonAnnotatedProviderClassPoweredProvider(Class<T> key, Class<? extends Provider<T>> providerClass) {
    this.key = key;
    this.providerClass = providerClass;
  }

  @Override
  public T get() {
    Factory<? extends Provider<T>> providerFactory = FactoryRegistry.getFactory(providerClass);
    getInjector().getScope().put(key, new ProducerFactoryPoweredProvider(providerFactory));
    return providerFactory.createInstance(getInjector()).get();
  }
}
