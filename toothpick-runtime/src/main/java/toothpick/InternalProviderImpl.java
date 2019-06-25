package toothpick;

import javax.inject.Provider;
import toothpick.locators.FactoryLocator;

/**
 * A non thread safe internal provider. It should never be exposed outside of Toothpick.
 *
 * @param <T> the class of the instances provided by this provider.
 */
public class InternalProviderImpl<T> {
  private volatile T instance;
  private Factory<T> factory;
  private Class<T> factoryClass;
  private volatile Provider<? extends T> providerInstance;
  private Factory<Provider<T>> providerFactory;
  private Class<Provider<T>> providerFactoryClass;

  protected boolean isProvidingSingletonInScope;
  private boolean isCreatingSingletonInScope;
  private boolean isReleasable;
  private boolean isProviderReleasable;

  public InternalProviderImpl(T instance) {
    //not that an instance cannot be releasable as TP wouldn't know how to recreate a second instance.
    if (instance == null) {
      throw new IllegalArgumentException("The instance can't be null.");
    }

    this.instance = instance;
  }

  public InternalProviderImpl(Provider<? extends T> providerInstance,
                              boolean isProvidingSingletonInScope,
                              boolean isProvidingReleasable) {
    //not that an instance of provider cannot be releasable as TP wouldn't know how to recreate a second instance.
    //but it can provide releasable singletons
    if (providerInstance == null) {
      throw new IllegalArgumentException("The provider can't be null.");
    }

    this.providerInstance = providerInstance;
    this.isProvidingSingletonInScope = isProvidingSingletonInScope;
    this.isReleasable = isProvidingReleasable;
  }

  public InternalProviderImpl(Factory<?> factory, boolean isProviderFactory) {
    if (factory == null) {
      throw new IllegalArgumentException("The factory can't be null.");
    }

    if (isProviderFactory) {
      this.providerFactory = (Factory<Provider<T>>) factory;
      isProviderReleasable = factory.hasProvidesReleasableAnnotation();
    } else {
      this.factory = (Factory<T>) factory;
      isReleasable = factory.hasReleasableAnnotation();
    }
  }

  public InternalProviderImpl(Class<?> factoryKeyClass,
      boolean isProviderFactoryClass,
      boolean isCreatingSingletonInScope,
      boolean isProviderReleasable,
      boolean isProvidingSingletonInScope,
      boolean isProvidingReleasable) {
    if (factoryKeyClass == null) {
      throw new IllegalArgumentException("The factory class can't be null.");
    }

    if (isProviderFactoryClass) {
      this.providerFactoryClass = (Class<Provider<T>>) factoryKeyClass;
    } else {
      this.factoryClass = (Class<T>) factoryKeyClass;
      this.isProviderReleasable = isProviderReleasable;
    }
    this.isCreatingSingletonInScope = isCreatingSingletonInScope;
    this.isProvidingSingletonInScope = isProvidingSingletonInScope;
    this.isReleasable = isProvidingReleasable;
  }

  //we lock on the unbound provider itself to prevent concurrent usage
  //of the unbound provider (
  public synchronized T get(Scope scope) {
    if (instance != null) {
      return instance;
    }

    if (providerInstance != null) {
      if (isProvidingSingletonInScope) {
        instance = providerInstance.get();
        //gc
        providerInstance = null;
        return instance;
      }

      return providerInstance.get();
    }

    if (factoryClass != null && factory == null) {
      factory = FactoryLocator.getFactory(factoryClass);
      //gc
      factoryClass = null;
    }

    if (factory != null) {
      if (!factory.hasSingletonAnnotation() && !isCreatingSingletonInScope) {
        return factory.createInstance(scope);
      }
      instance = factory.createInstance(scope);
      //gc
      factory = null;
      return instance;
    }

    if (providerFactoryClass != null && providerFactory == null) {
      providerFactory = FactoryLocator.getFactory(providerFactoryClass);
      //gc
      providerFactoryClass = null;
    }

    if (providerFactory != null) {
      if (providerFactory.hasProvidesSingletonInScopeAnnotation() || isProvidingSingletonInScope) {
        instance = providerFactory.createInstance(scope).get();
        //gc
        providerFactory = null;
        return instance;
      }
      if (providerFactory.hasSingletonAnnotation() || isCreatingSingletonInScope) {
        providerInstance = providerFactory.createInstance(scope);
        //gc
        providerFactory = null;
        return providerInstance.get();
      }

      return providerFactory.createInstance(scope).get();
    }

    throw new IllegalStateException("A provider can only be used with an instance, a provider, a factory or a provider factory. Should not happen.");
  }

  public boolean isReleasable() {
    return isReleasable;
  }

  public void release() {
    //gc
    instance = null;
    providerInstance = null;
  }
}