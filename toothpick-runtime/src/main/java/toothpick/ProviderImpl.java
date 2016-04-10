package toothpick;

import javax.inject.Provider;

//TODO only the provider created by injector.getProvider need to be thread safe, all others
//are already accessed with a lock
public class ProviderImpl<T> implements Provider<T>, Lazy<T> {
  private Injector injector;
  private volatile T instance;
  private Factory<T> factory;
  private volatile Provider<T> providerInstance;
  private boolean isLazy;
  private Factory<Provider<T>> providerFactory;

  public ProviderImpl(T instance) {
    this.instance = instance;
  }

  public ProviderImpl(Provider<T> providerInstance, boolean isLazy) {
    this.providerInstance = providerInstance;
    this.isLazy = isLazy;
  }

  public ProviderImpl(Injector injector, Factory<?> factory, boolean isProviderFactory) {
    this.injector = injector;
    if (isProviderFactory) {
      this.providerFactory = (Factory<Provider<T>>) factory;
    } else {
      this.factory = (Factory<T>) factory;
    }
  }

  @Override
  public T get() {
    if (instance != null) {
      return instance;
    }
    if (providerInstance != null) {
      if (isLazy) {
        //DCL
        if (instance == null) {
          synchronized (this) {
            instance = providerInstance.get();
          }
        }
        return instance;
      }
      //to ensure the wrapped provider doesn't have to deal
      //with concurrency
      synchronized (this) {
        return providerInstance.get();
      }
    }
    if (factory != null) {
      if (!factory.hasSingletonAnnotation()) {
        return factory.createInstance(injector);
      }
      //DCL
      if (instance == null) {
        synchronized (this) {
          instance = factory.createInstance(injector);
        }
      }
      return instance;
    }
    if (providerFactory != null) {
      if (providerFactory.hasProducesSingletonAnnotation()) {
        //DCL
        if (instance == null) {
          synchronized (this) {
            instance = providerFactory.createInstance(injector).get();
          }
        }
        return instance;
      }
      if (providerFactory.hasSingletonAnnotation()) {
        if (providerInstance == null) {
          //DCL
          synchronized (this) {
            providerInstance = providerFactory.createInstance(injector);
          }
        }
        //to ensure the wrapped provider doesn't have to deal
        //with concurrency
        synchronized (this) {
          return providerInstance.get();
        }
      }
      Provider<T> provider = providerFactory.createInstance(injector);
      //to ensure the wrapped provider doesn't have to deal
      //with concurrency
      synchronized (this) {
        return provider.get();
      }
    }
    throw new IllegalStateException("A provider can only be used with an instance, a provider, a factory or a provider factory.");
  }
}