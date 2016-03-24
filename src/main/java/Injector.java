/**
 * Allows to inject members of a given instance,
 * create instances of a given class.
 * An injector has its own scope.
 */
public interface Injector {
  void inject(Object obj);
  <T> T createInstance(Class<T> clazz);
}
