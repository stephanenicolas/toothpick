package toothpick;

import javax.inject.Provider;
import toothpick.registries.factory.FactoryRegistryLocator;

public class ProviderImpl<T> implements Provider<T>, Lazy<T> {
  private Scope scope;
  private T instance;
  private Factory<T> factory;
  private Class<T> factoryClass;
  private Provider<T> providerInstance;
  private boolean isLazy;
  private Factory<Provider<T>> providerFactory;
  private Class<Provider<T>> providerFactoryClass;

  public ProviderImpl(T instance) {
    this.instance = instance;
  }

  public ProviderImpl(Provider<T> providerInstance, boolean isLazy) {
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
        return instance = providerInstance.get();
      }
      return providerInstance.get();
    }

    if(factoryClass != null && factory == null) {
      factory = FactoryRegistryLocator.getFactory(factoryClass);
    }

    if (factory != null) {
      if (!factory.hasSingletonAnnotation()) {
        return factory.createInstance(scope);
      }
      return instance = factory.createInstance(scope);
    }

    if(providerFactoryClass != null && providerFactory == null) {
      providerFactory = FactoryRegistryLocator.getFactory(providerFactoryClass);
    }

    if (providerFactory != null) {
      if (providerFactory.hasProducesSingletonAnnotation()) {
        return instance = providerFactory.createInstance(scope).get();
      }
      if (providerFactory.hasSingletonAnnotation()) {
        return (providerInstance = providerFactory.createInstance(scope)).get();
      }

      return providerFactory.createInstance(scope).get();
    }

    throw new IllegalStateException("A provider can only be used with an instance, a provider, a factory or a provider factory.");
  }
}