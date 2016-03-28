package toothpick.providers;

import toothpick.Factory;
import toothpick.FactoryRegistry;
import toothpick.InjectorImpl;

/**
 * A producer that uses a {@link javax.inject.Singleton} annotated class to produces instances of {@code T}.
 * The producer will always ask the factory to create an instance of {@code T},
 * then we don't use the factory any more and return the singleton.
 *
 * @param <T> the type of the instances provided by this provider.
 */
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
