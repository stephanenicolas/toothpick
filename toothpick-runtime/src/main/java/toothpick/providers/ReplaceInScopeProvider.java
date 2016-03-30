package toothpick.providers;

import javax.inject.Provider;
import toothpick.InjectorImpl;

/**
 * Base class of providers that replace themselves in the scope with a more efficient provider.
 * @param <T> the type of the instances provided by this provider.
 */
public abstract class ReplaceInScopeProvider<T> implements Provider<T> {
  private InjectorImpl injector;
  private Class<T> key;

  public ReplaceInScopeProvider(InjectorImpl injector, Class<T> key) {
    this.injector = injector;
    this.key = key;
  }

  public <T> void replaceInScope(Provider<T> newProvider) {
    getInjector().getScope().put(key, newProvider);
  }

  protected InjectorImpl getInjector() {
    return injector;
  }
}
