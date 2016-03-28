package toothpick.providers;

import toothpick.Factory;
import toothpick.FactoryRegistry;
import toothpick.InjectorImpl;

public class SingletonAnnotatedClassPoweredProvider<T> extends BaseProvider<T> {
  private Class<T> key;
  private Class<T> clazz;

  public SingletonAnnotatedClassPoweredProvider(InjectorImpl injector, Class<T> key, Class<T> clazz) {
    super(injector);
    this.key = key;
    this.clazz = clazz;
  }

  @Override public T get() {
    Factory<T> factory = FactoryRegistry.getFactory(clazz);
    T instance = factory.createInstance(getInjector());
    getInjector().getScope().put(key, new SingletonPoweredProvider(instance));
    return instance;
  }
}
