package toothpick;

import javax.inject.Provider;
import toothpick.registries.factory.FactoryRegistryLocator;

/**
 * A non thread safe internal provider. It should never be exposed outside of ToothPick.
 *
 * @param <T> the class of the instances provided by this provider.
 */
public class InternalProviderImpl<T> {
  private T instance;
  private Factory<T> factory;
  private Class<T> factoryClass;
  private Provider<? extends T> providerInstance;
  private boolean isLazy;
  private Factory<Provider<T>> providerFactory;
  private Class<Provider<T>> providerFactoryClass;

  public InternalProviderImpl(T instance) {
    this.instance = instance;
  }

  public InternalProviderImpl(Provider<? extends T> providerInstance, boolean isLazy) {
    this.providerInstance = providerInstance;
    this.isLazy = isLazy;
  }

  public InternalProviderImpl(Factory<?> factory, boolean isProviderFactory) {
    if (isProviderFactory) {
      this.providerFactory = (Factory<Provider<T>>) factory;
    } else {
      this.factory = (Factory<T>) factory;
    }
  }

  public InternalProviderImpl(Class<?> factoryKeyClass, boolean isProviderFactoryClass) {
    if (isProviderFactoryClass) {
      this.providerFactoryClass = (Class<Provider<T>>) factoryKeyClass;
    } else {
      this.factoryClass = (Class<T>) factoryKeyClass;
    }
  }

  //we lock on the unbound provider itself to prevent concurrent usage
  //of the unbound provider (
  public synchronized T get(Scope scope) {
    if (instance != null) {
      return instance;
    }

    if (providerInstance != null) {
      if (isLazy) {
        instance = providerInstance.get();
        //gc
        providerInstance = null;
        return instance;
      }
      return providerInstance.get();
    }

    if (factoryClass != null && factory == null) {
      factory = FactoryRegistryLocator.getFactory(factoryClass);
      //gc
      factoryClass = null;
    }

    if (factory != null) {
      if (!factory.hasScopeAnnotation()) {
        return factory.createInstance(scope);
      }
      Scope targetScope = factory.getTargetScope(scope);
      instance = factory.createInstance(targetScope);
      //gc
      factory = null;
      return instance;
    }

    if (providerFactoryClass != null && providerFactory == null) {
      providerFactory = FactoryRegistryLocator.getFactory(providerFactoryClass);
      //gc
      providerFactoryClass = null;
    }

    if (providerFactory != null) {
      if (providerFactory.hasScopeInstancesAnnotation()) {
        instance = providerFactory.createInstance(scope).get();
        //gc
        providerFactory = null;
        return instance;
      }
      if (providerFactory.hasScopeAnnotation()) {
        Scope targetScope = providerFactory.getTargetScope(scope);
        providerInstance = providerFactory.createInstance(targetScope);
        //gc
        providerFactory = null;
        return providerInstance.get();
      }

      return providerFactory.createInstance(scope).get();
    }

    throw new IllegalStateException("A provider can only be used with an instance, a provider, a factory or a provider factory.");
  }
}