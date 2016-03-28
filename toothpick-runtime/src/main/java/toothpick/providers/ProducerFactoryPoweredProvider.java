package toothpick.providers;

import toothpick.Factory;
import toothpick.InjectorImpl;
import toothpick.Provider;

public class ProducerFactoryPoweredProvider<T> extends BaseProvider<T> {
  private Factory<? extends Provider<T>> providerFactory;

  public ProducerFactoryPoweredProvider(InjectorImpl injector, Factory<? extends Provider<T>> providerFactory) {
    super(injector);
    this.providerFactory = providerFactory;
  }

  @Override public T get() {
    return providerFactory.createInstance(getInjector()).get();
  }
}
