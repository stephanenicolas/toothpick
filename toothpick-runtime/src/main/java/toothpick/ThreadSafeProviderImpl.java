package toothpick;

import java.lang.ref.WeakReference;
import javax.inject.Provider;

/**
 * A thread safe internal provider. It will be exposed outside of Toothpick.
 *
 * @param <T> the class of the instances provided by this provider.
 */
public class ThreadSafeProviderImpl<T> implements Provider<T>, Lazy<T> {
  private volatile T instance;
  private WeakReference<Scope> scope;
  private InternalProviderImpl<? extends T> providerInstance;
  private boolean isLazy;

  public ThreadSafeProviderImpl(Scope scope, InternalProviderImpl<? extends T> providerInstance, boolean isLazy) {
    this.scope = new WeakReference<>(scope);
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
          instance = providerInstance.get(getScope());
          //gc
          providerInstance = null;
        }
        return instance;
      }
      return providerInstance.get(getScope());
    }
  }

  private Scope getScope() {
    final Scope scope = this.scope.get();
    if (scope == null) {
      throw new IllegalStateException(String.format("The instance provided by the %s "
              + "cannot be created when the associated scope has been closed", isLazy ? "lazy" : "provider"));
    }
    return scope;
  }
}