package toothpick;

import javax.inject.Inject;

/**
 * Creates instances of classes.
 * Factories are discovered via a {@link FactoryRegistry}.
 * Implementations are generated during annotation processing.
 * As soon as a class as an {@link javax.inject.Inject} annotated constructor,
 * a factory is created. All classes that need to be created via toothpick
 * need an annotated constructor, other we will fall back on reflection and emit
 * a warning at runtime.
 * There can be one and only one annotated constructor.
 *
 * If a factory detects that a {@code T} has {@javax.inject.Inject} annotated fields,
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
   * @return the name of the scope in which all instances of this {@code Factory} should be
   * created and where instances of {@code T} will be recycled.
   */
  String getScopeName();

  /**
   * Signals that the class is anotated with {@link ProvidesSingleton}.
   *
   * @return true iff the class is annotated as a producer class whose instances will produce a singleton.
   */
  boolean hasProducesSingletonAnnotation();
}
