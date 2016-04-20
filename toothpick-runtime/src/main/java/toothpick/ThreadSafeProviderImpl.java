package toothpick;

import javax.inject.Provider;

/**
 * A thread safe internal provider. It will be exposed outside of ToothPick.
 * @param <T> the class of the instances provided by this provider.
 */
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
    //the first test avoids accessing a volatile when not needed
    if (isLazy && instance != null) {
      return instance;
    }

    //ensure both sync for DSL
    //and sync around provider
    //so that dev's providers don't deal with concurrency
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