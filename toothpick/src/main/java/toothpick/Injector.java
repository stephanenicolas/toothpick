package toothpick;

/**
 * Allows to inject members of a given instance.
 * An scope works with a scope.
 */
public interface Injector {
  /**
   * Injects all fields of an object. This object will be the starting point of an injection sub-graph, i.e.
   * all dependencies of this object will be injected as well when created.
   *
   * @param obj the object of which to all fields will be injected.
   * @param <T> the type of {@code clazz}.
   */
  <T> void inject(T obj, Scope scope);

  <T> void inject(Class<T> clazz, T obj, Scope scope);
}
