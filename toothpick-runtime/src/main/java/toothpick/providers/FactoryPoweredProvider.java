package toothpick.providers;

import toothpick.Factory;
import toothpick.InjectorImpl;

/**
 * A provider that uses a {@link Factory} to provide instances.
 * It will always us the factory to create new instances of {@code T}.
 * @param <T> the type of the instances provided by this provider.
 */
public final class FactoryPoweredProvider<T> extends InScopeProvider<T> {
  private Factory<T> factory;

  public FactoryPoweredProvider(InjectorImpl injector, Factory<T> factory) {
    super(injector);
    this.factory = factory;
  }

  @Override public T get() {
    return factory.createInstance(getInjector());
  }
}
