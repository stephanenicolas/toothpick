package toothpick.providers;

import javax.inject.Provider;
import toothpick.Factory;
import toothpick.InjectorImpl;

/**
 * A producer that uses a {@link Factory} of providers to produces instances of {@code T}.
 * The producer will always ask the factory to create a provider and ask the provider to create an instance of {@code T}.
 * @param <T> the type of the instances provided by this provider.
 */
public class ProducerFactoryPoweredProvider<T> extends InScopeProvider<T> {
  private Factory<? extends Provider<T>> providerFactory;

  public ProducerFactoryPoweredProvider(InjectorImpl injector, Factory<? extends Provider<T>> providerFactory) {
    super(injector);
    this.providerFactory = providerFactory;
  }

  @Override public T get() {
    return providerFactory.createInstance(getInjector()).get();
  }
}
