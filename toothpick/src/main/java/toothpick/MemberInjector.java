package toothpick;

/**
 * Inject member of an instance of a class.
 * All injected members are gonna be obtained in the scope of the current injector.
 * MemberInjector are discovered via a {@link MemberInjectorRegistry}.
 * Implementations are generated during annotation processing.
 * As soon as a class as an {@link javax.inject.Inject} annotated field or method,
 * a member injector is created. All classes that need to be injected via toothpick
 * need to be package private, otherwise we will fall back on reflection and emit
 * a warning at runtime.
 *
 * @See MemberInjectorRegistry
 */
public interface MemberInjector<T> {
  /**
   * Injects all fields of an object. This object will be the starting point of an injection sub-graph.
   *
   * @param t the object in which to inject all dependencies.
   * @param injector/scope in which all dependencies will be looked for.
   */
  void inject(T t, Injector injector);
}
