package toothpick.providers;

import toothpick.Factory;
import toothpick.Injector;
import toothpick.Provider;

/**
 * Created by snicolas on 3/24/16.
 */
public class ProducerFactoryPoweredProvider<T> extends BaseProvider<T> {
  private Factory<? extends Provider<T>> providerFactory;

  public ProducerFactoryPoweredProvider(Factory<? extends Provider<T>> providerFactory) {
    this.providerFactory = providerFactory;
  }

  @Override
  public T get() {
    return providerFactory.createInstance(getInjector()).get();
  }
}
