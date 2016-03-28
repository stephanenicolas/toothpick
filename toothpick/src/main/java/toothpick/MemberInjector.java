package toothpick;

/**
 * Creates instance of classes.
 * @See FactoryRegistry
 * TODO document properly.
 */
public interface MemberInjector<T> {
  /**
   * Injects all fields of an object. This object will be the starting point of an injection sub-graph, i.e.
   * all dependencies of this object will be injected as well when created.
   * @param t the object of which to inject all dependencies.
   * @param injector/scope in which all dependencies will be looked for.
   */
  void inject(T t, Injector injector);
}
