package toothpick;

import javax.inject.Provider;

public class ThreadSafeProviderImpl<T> implements Provider<T>, Lazy<T> {
  private volatile T instance;
  private Provider<T> providerInstance;
  private boolean isLazy;

  public ThreadSafeProviderImpl(Provider<T> providerInstance, boolean isLazy) {
    this.providerInstance = providerInstance;
    this.isLazy = isLazy;
  }

  @Override
  public T get() {
    if (instance != null) {
      return instance;
    }

    //ensure both sync for DSL
    //and sync around provider
    //so that devs providers don't deal with concurrency
    synchronized (this) {
      if (isLazy) {
        //DCL
        if (instance == null) {
          instance = providerInstance.get();
        }
        return instance;
      }
      return providerInstance.get();
    }
  }
}