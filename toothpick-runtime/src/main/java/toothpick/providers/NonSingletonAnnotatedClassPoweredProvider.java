package toothpick.providers;

import toothpick.Factory;
import toothpick.FactoryRegistry;
import toothpick.InjectorImpl;

public class NonSingletonAnnotatedClassPoweredProvider<T, IMPL extends T> extends BaseProvider<T> {
  private Class<T> key;
  private Class<IMPL> implClass;

  public NonSingletonAnnotatedClassPoweredProvider(InjectorImpl injector, Class<T> key, Class<IMPL> implClass) {
    super(injector);
    this.key = key;
    this.implClass = implClass;
  }

  @Override public T get() {
    Factory<IMPL> factory = FactoryRegistry.getFactory(implClass);
    IMPL instance = factory.createInstance(getInjector());
    getInjector().getScope().put(key, new FactoryPoweredProvider<>(getInjector(), factory));
    return instance;
  }
}
