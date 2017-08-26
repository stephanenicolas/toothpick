package toothpick;

import javax.inject.Inject;

/**
 * Creates instances of classes.
 * Factories are discovered via a {@code FactoryRegistry}.
 * Implementations are generated during annotation processing.
 * As soon as a class as an {@link javax.inject.Inject} annotated constructor,
 * a factory is created. All classes that need to be created via toothpick
 * need an annotated constructor, other we will fall back on reflection and emit
 * a warning at runtime.
 * There can be one and only one annotated constructor.
 *
 * If a factory detects that a {@code T} has {@link javax.inject.Inject} annotated fields,
 * or one of its super classes, then it will inject the created instance of {@code T}.
 */
public interface Factory<T> {
  /**
   * Creates a new instance of T using its {@link Inject} annotated
   * constructor. There must be one and only annotated constructor.
   * If T has {@link Inject} annotated fields, then the new instance will be injected after creation.
   *
   * @param scope the scope in which to look for all dependencies of the instance T.
   * @return a new instance of T, injected if needed.
   */
  T createInstance(Scope scope);

  /**
   * This method will return the scope where {@code T} instances will be created and
   * where instances of {@code T} will be recycled if {@link #hasScopeAnnotation()}
   * return {@code true}.
   *
   * Given a {@code currentScope}, the factory can return either :
   * <ul>
   * <li> the scope itself (if class {@code T} is not annotated.
   * <li> the root scope if the class {@code T} is annotated with {@link javax.inject.Singleton}.
   * <li> a parent scope if the class {@code T} is annotated with an different scope annotation
   * (i.e. an annotation qualified by {@link javax.inject.Scope}).
   * </ul>
   *
   * @param currentScope the current scope used to create an instance.
   * @return the scope in which all instances produced by this {@code Factory} should be
   * created.
   */
  Scope getTargetScope(Scope currentScope);

  /**
   * Signals that the class is annotated with an annotation that is qualified by {@link javax.inject.Scope}.
   *
   * @return true iff the class is annotated with an annotation that is qualified by {@link javax.inject.Scope}.
   */
  boolean hasScopeAnnotation();

  /**
   * Signals that the class is anotated with {@link ProvidesSingletonInScope}.
   *
   * @return true iff the class is annotated as a producer class whose instances will produce a singleton.
   */
  boolean hasProvidesSingletonInScopeAnnotation();
}
