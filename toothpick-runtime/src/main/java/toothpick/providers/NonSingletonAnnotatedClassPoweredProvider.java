package toothpick.providers;

import toothpick.Factory;
import toothpick.InjectorImpl;
import toothpick.registries.factory.FactoryRegistryLocator;

/**
 * A provider that provides instances of a class {@code T} that is not annotated with {@link javax.inject.Singleton}.
 * The provider will always ask the factory associated with this {@code T} to create new instances of {@code T}.
 *
 * @param <T> the type of the instances provided by this provider.
 */
public class NonSingletonAnnotatedClassPoweredProvider<T, IMPL extends T> extends ReplaceInScopeProvider<T> {
  private Class<IMPL> implClass;

  public NonSingletonAnnotatedClassPoweredProvider(InjectorImpl injector, Class<T> key, Class<IMPL> implClass) {
    super(injector, key);
    this.implClass = implClass;
  }

  @Override public T get() {
    Factory<IMPL> factory = FactoryRegistryLocator.getFactory(implClass);
    IMPL instance = factory.createInstance(getInjector());
    replaceInScope(new FactoryPoweredProvider<>(getInjector(), factory));
    return instance;
  }
}
