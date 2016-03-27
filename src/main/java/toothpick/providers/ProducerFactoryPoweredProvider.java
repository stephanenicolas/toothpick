package toothpick.providers;

import toothpick.Factory;
import toothpick.Injector;
import toothpick.Provider;

public class ProducerFactoryPoweredProvider<T> extends BaseProvider<T> {
  private Factory<? extends Provider<T>> providerFactory;

  public ProducerFactoryPoweredProvider(Factory<? extends Provider<T>> providerFactory) {
    this.providerFactory = providerFactory;
  }

  @Override public T get() {
    return providerFactory.createInstance(getInjector()).get();
  }
}
