package toothpick;

/**
 * Allows to inject members of a given instance,
 * create instances of a given class.
 * An injector has its own scope.
 */
public interface Injector {
  Object getKey();

  <T> T getScopedInstance(Class<T> clazz);

  <T> void inject(T obj);

  <T> T createInstance(Class<T> clazz);

  Injector getParent();
}
