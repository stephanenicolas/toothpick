package toothpick.providers;

import javax.inject.Provider;
import toothpick.InjectorImpl;

/**
 * Base class of providers that needs a scope to produce instances.
 * All creation of instances require a scope, except for given singletons.
 * @param <T> the type of the instances provided by this provider.
 */
public abstract class InScopeProvider<T> implements Provider<T> {
  private InjectorImpl injector;

  public InScopeProvider(InjectorImpl injector) {
    this.injector = injector;
  }

  protected InjectorImpl getInjector() {
    return injector;
  }
}
