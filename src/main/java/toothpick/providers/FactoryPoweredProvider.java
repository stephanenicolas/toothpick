package toothpick.providers;

import toothpick.Factory;
import toothpick.Injector;
import toothpick.Provider;

/**
 * Created by snicolas on 3/24/16.
 */
public class FactoryPoweredProvider<T> extends BaseProvider<T> {
  private Factory<T> factory;

  public FactoryPoweredProvider(Factory<T> factory, Injector injector) {
    this.factory = factory;
    setInjector(injector);
  }

  public FactoryPoweredProvider(Factory<T> factory) {
    this.factory = factory;
  }

  @Override public T get() {
    return factory.createInstance(getInjector());
  }
}
