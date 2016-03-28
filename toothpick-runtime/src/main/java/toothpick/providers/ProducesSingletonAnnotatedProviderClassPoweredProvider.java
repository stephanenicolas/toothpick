package toothpick.providers;

import toothpick.Factory;
import toothpick.FactoryRegistry;
import toothpick.InjectorImpl;
import toothpick.ProvidesSingleton;
import toothpick.Provider;

/**
 * A producer that uses a {@link Factory} of providers to produces instances of {@code T}.
 * The provider class is annotated with {@link ProvidesSingleton}.
 * The producer will always ask the factory to create a provider first and
 * ask the provider to create an instance of {@code T}, then we don't use the provider
 * any more and return the singleton.
 * @param <T> the type of the instances provided by this provider.
 */
public class ProducesSingletonAnnotatedProviderClassPoweredProvider<T> extends ReplaceInScopeProvider<T> {
  private Class<? extends Provider<? extends T>> providerClass;

  public ProducesSingletonAnnotatedProviderClassPoweredProvider(InjectorImpl injector, Class<T> key,
      Class<? extends Provider<? extends T>> providerClass) {
    super(injector, key);
    this.providerClass = providerClass;
  }

  @Override public T get() {
    Factory<? extends Provider<? extends T>> providerFactory =
        FactoryRegistry.getFactory(providerClass);
    Provider<? extends T> provider = providerFactory.createInstance(getInjector());
    T instance = provider.get();
    replaceInScope(new SingletonPoweredProvider<>(instance));
    return null;
  }
}
