package toothpick.providers;

import javax.inject.Provider;
import toothpick.Factory;
import toothpick.InjectorImpl;
import toothpick.registries.FactoryRegistryLocator;

/**
 * A producer that uses a {@link Factory} of providers to produces instances of {@code T}.
 * The provider class is annotated with {@link javax.inject.Singleton}.
 * The producer will ask the factory of the provider to create a provider first and
 * ask the provider to create an instance of {@code T}, then we don't use the factory
 * any more and return an instance produced by the singleton provider.
 *
 * @param <T> the type of the instances provided by this provider.
 */
public class SingletonAnnotatedProviderClassPoweredProvider<T> extends ReplaceInScopeProvider<T> {
  private Class<? extends Provider<? extends T>> providerClass;

  public SingletonAnnotatedProviderClassPoweredProvider(InjectorImpl injector, Class<T> key, Class<? extends Provider<? extends T>> providerClass) {
    super(injector, key);
    this.providerClass = providerClass;
  }

  @Override public T get() {
    Factory<? extends Provider<? extends T>> providerFactory = FactoryRegistryLocator.getFactory(providerClass);
    Provider<? extends T> provider = providerFactory.createInstance(getInjector());
    replaceInScope(provider);
    return provider.get();
  }
}
