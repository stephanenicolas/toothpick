package toothpick.providers;

import toothpick.Factory;
import toothpick.InjectorImpl;

public class FactoryPoweredProvider<T> extends BaseProvider<T> {
  private Factory<T> factory;

  public FactoryPoweredProvider(InjectorImpl injector, Factory<T> factory) {
    super(injector);
    this.factory = factory;
  }

  @Override public T get() {
    return factory.createInstance(getInjector());
  }
}
