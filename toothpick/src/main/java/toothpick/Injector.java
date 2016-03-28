package toothpick;

/**
 * Allows to inject members of a given instance,
 * create instances of a given class.
 * An injector has its own scope.
 */
public interface Injector {
  /**
   * The key that identifies the injector in toothpick.
   * @return the key of this injector.
   */
  Object getKey();

  /**
   * Injects all fields of an object. This object will be the starting point of an injection sub-graph, i.e.
   * all dependencies of this object will be injected as well when created.
   * @param obj the object of which to all fields will be injected.
   * @param <T> the type of {@code clazz}.
   */
  <T> void inject(T obj);

  /**
   * Returns the instance of {@code clazz} if one is scoped in the current
   * scope, or its ancestors. If there is no such instance, the factory associated
   * to the clazz will be used.
   * TODO : in case no factory is found, fallback on reflection and emit a slow down warning.
   * All {@link javax.inject.Inject} annotated fields of the instance are injected after creation.
   * If the {@param clazz} is annotated with {@link javax.inject.Singleton} then the created instance
   * will be scoped in the current scope.
   * @param clazz the class for which to obtain an instance in the scope of this injector.
   * @param <T> the type of {@code clazz}.
   * @return a scoped instance or a new one produced by the factory associated to {@code clazz}.
   */
  <T> T createInstance(Class<T> clazz);

  /**
   * @return the parent of this injector. Can be null for a root injector.
   */
  Injector getParent();
}
