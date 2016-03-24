package toothpick.providers;

import toothpick.Injector;
import toothpick.InjectorImpl;
import toothpick.Provider;

/**
 * Created by snicolas on 3/24/16.
 */
public abstract class BaseProvider<T> implements Provider<T> {
  private InjectorImpl injector;

  @Override
  public void setInjector(Injector injector) {
    this.injector = (InjectorImpl) injector;
  }

  protected InjectorImpl getInjector() {
    return injector;
  }
}
