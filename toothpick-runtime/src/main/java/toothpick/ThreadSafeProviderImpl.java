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
  private String scopeName;
  private Class<T> clazz;
  private String name;
  private boolean isLazy;

  public ThreadSafeProviderImpl(Scope scope, Class<T> clazz, String name, boolean isLazy) {
    this.scope = new WeakReference<>(scope);
    this.scopeName = scope.getName().toString();
    this.clazz = clazz;
    this.name = name;
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
          instance = getScope().getInstance(clazz, name);
          scope.clear();
        }
        return instance;
      }
      return getScope().getInstance(clazz, name);
    }
  }

  private Scope getScope() {
    final Scope scope = this.scope.get();
    if (scope == null) {
      throw new IllegalStateException(String.format("The instance provided by the %s "
              + "cannot be created when the associated scope: %s has been closed",
          isLazy ? "lazy" : "provider",
          scopeName));
    }
    return scope;
  }
}