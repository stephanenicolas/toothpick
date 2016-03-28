package toothpick.providers;

import toothpick.Injector;
import toothpick.InjectorImpl;
import toothpick.Provider;

public abstract class BaseProvider<T> implements Provider<T> {
  private InjectorImpl injector;

  @Override public void setInjector(Injector injector) {
    this.injector = (InjectorImpl) injector;
  }

  protected InjectorImpl getInjector() {
    return injector;
  }
}
