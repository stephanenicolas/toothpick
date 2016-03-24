package toothpick;

/**
 * Allows to inject members of a given instance,
 * create instances of a given class.
 * An injector has its own scope.
 */
public interface Injector {
  Object getKey();

  <T> T getScopedInstance(Class<T> clazz);

  void inject(Object obj);
  <T> T createInstance(Class<T> clazz);

  Injector getParent();
}
