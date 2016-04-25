package toothpick;

import javax.inject.Provider;
import toothpick.registries.factory.FactoryRegistryLocator;

/**
 * A non thread safe internal provider. It should never be exposed outside of ToothPick.
 *
 * @param <T> the class of the instances provided by this provider.
 */
public class ProviderImpl<T> implements Provider<T>, Lazy<T> {
  private Scope scope;
  private T instance;
  private Factory<T> factory;
  private Class<T> factoryClass;
  private Provider<? extends T> providerInstance;
  private boolean isLazy;
  private Factory<Provider<T>> providerFactory;
  private Class<Provider<T>> providerFactoryClass;

  public ProviderImpl(T instance) {
    this.instance = instance;
  }

  public ProviderImpl(Provider<? extends T> providerInstance, boolean isLazy) {
    this.providerInstance = providerInstance;
    this.isLazy = isLazy;
  }

  public ProviderImpl(Scope scope, Factory<?> factory, boolean isProviderFactory) {
    this.scope = scope;
    if (isProviderFactory) {
      this.providerFactory = (Factory<Provider<T>>) factory;
    } else {
      this.factory = (Factory<T>) factory;
    }
  }

  public ProviderImpl(Scope scope, Class<?> factoryKeyClass, boolean isProviderFactoryClass) {
    this.scope = scope;
    if (isProviderFactoryClass) {
      this.providerFactoryClass = (Class<Provider<T>>) factoryKeyClass;
    } else {
      this.factoryClass = (Class<T>) factoryKeyClass;
    }
  }

  @Override
  public T get() {
    if (instance != null) {
      return instance;
    }

    if (providerInstance != null) {
      if (isLazy) {
        instance = providerInstance.get();
        return instance;
      }
      return providerInstance.get();
    }

    if (factoryClass != null && factory == null) {
      factory = FactoryRegistryLocator.getFactory(factoryClass);
    }

    if (factory != null) {
      if (!factory.hasScopeAnnotation()) {
        return factory.createInstance(scope);
      }
      instance = factory.createInstance(scope);
      return instance;
    }

    if (providerFactoryClass != null && providerFactory == null) {
      providerFactory = FactoryRegistryLocator.getFactory(providerFactoryClass);
    }

    if (providerFactory != null) {
      if (providerFactory.hasProducesSingletonAnnotation()) {
        instance = providerFactory.createInstance(scope).get();
        return instance;
      }
      if (providerFactory.hasScopeAnnotation()) {
        providerInstance = providerFactory.createInstance(scope);
        return providerInstance.get();
      }

      return providerFactory.createInstance(scope).get();
    }

    throw new IllegalStateException("A provider can only be used with an instance, a provider, a factory or a provider factory.");
  }
}