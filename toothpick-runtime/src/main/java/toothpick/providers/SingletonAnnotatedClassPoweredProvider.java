package toothpick.providers;

import toothpick.Factory;
import toothpick.FactoryRegistry;
import toothpick.InjectorImpl;

public class SingletonAnnotatedClassPoweredProvider<T> extends ReplaceInScopeProvider<T> {
  private Class<T> clazz;

  public SingletonAnnotatedClassPoweredProvider(InjectorImpl injector, Class<T> key, Class<T> clazz) {
    super(injector, key);
    this.clazz = clazz;
  }

  @Override public T get() {
    Factory<T> factory = FactoryRegistry.getFactory(clazz);
    T instance = factory.createInstance(getInjector());
    replaceInScope(new SingletonPoweredProvider(instance));
    return instance;
  }
}
