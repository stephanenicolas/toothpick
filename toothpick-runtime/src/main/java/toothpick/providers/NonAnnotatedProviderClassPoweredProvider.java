package toothpick.providers;

import toothpick.Factory;
import toothpick.FactoryRegistry;
import toothpick.InjectorImpl;
import toothpick.ProducesSingleton;
import toothpick.Provider;

/**
 * A provider that uses a {@link Factory} of provider to provide instances.
 * The provider class should not be annotated with {@link ProducesSingleton}
 * or {@link @Singleton}.
 * The provider will first create a provider using the factory, then ask it to provide
 * an instance of {@code T}.
 * @param <T> the type of the instances provided by this provider.
 */
public class NonAnnotatedProviderClassPoweredProvider<T> extends ReplaceInScopeProvider<T> {
  private final Class<? extends Provider<T>> providerClass;

  public NonAnnotatedProviderClassPoweredProvider(InjectorImpl injector, Class<T> key,
      Class<? extends Provider<T>> providerClass) {
    super(injector, key);
    this.providerClass = providerClass;
  }

  @Override public T get() {
    Factory<? extends Provider<T>> providerFactory = FactoryRegistry.getFactory(providerClass);
    replaceInScope(new ProducerFactoryPoweredProvider(getInjector(), providerFactory));
    return providerFactory.createInstance(getInjector()).get();
  }
}
