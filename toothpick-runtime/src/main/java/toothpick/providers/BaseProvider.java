package toothpick.providers;

import toothpick.InjectorImpl;
import toothpick.Provider;

public abstract class BaseProvider<T> implements Provider<T> {
  private InjectorImpl injector;

  public BaseProvider(InjectorImpl injector) {
    this.injector = injector;
  }

  protected InjectorImpl getInjector() {
    return injector;
  }
}
