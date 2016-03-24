package toothpick.providers;

import toothpick.Factory;
import toothpick.FactoryRegistry;
import toothpick.Provider;

/**
 * Created by snicolas on 3/24/16.
 */
public class NonSingletonAnnotatedClassPoweredProvider<T, IMPL extends T> extends BaseProvider<T> {
  private Class<T> key;
  private Class<IMPL> implClass;

  public NonSingletonAnnotatedClassPoweredProvider(Class<T> key, Class<IMPL> implClass) {
    this.key = key;
    this.implClass = implClass;
  }

  @Override public T get() {
    Factory<IMPL> factory = FactoryRegistry.getFactory(implClass);
    IMPL instance = factory.createInstance(getInjector());
    getInjector().getScope().put(key, new FactoryPoweredProvider<>(factory, getInjector()));
    return instance;
  }
}
