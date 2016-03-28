package toothpick;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Creates instances of classes.
 * Factories are discovered via a {@link FactoryRegistry}.
 * Implementations are generated during annotation processing.
 * As soon as a class as an {@link javax.inject.Inject} annotated constructor,
 * a factory is created. All classes that need to be created via toothpick
 * need an annotated constructor, other we will fall back on reflection and emit
 * a warning at runtime.
 * There can be one and only one annotated constructor.
 */
public interface Factory<T> {
  /**
   * Creates a new instance of T using its {@link Inject} annotated
   * constructor. There must be one and only annotated constructor.
   * TODO : fallback on reflection to call default constuctor and emit a slow down warning.
   * If T has {@link Inject} annotated fields, then the new instance will be injected after creation.
   * @param injector the scope/injector in which to look for all dependencies of the instance T.
   * @return a new instance of T, injected if needed.
   */
  T createInstance(Injector injector);

  /**
   * Signals that the class is annotated with {@link Singleton}.
   * @return true iff the class is annotated as a singleton.
   */
  boolean hasSingletonAnnotation();

  /**
   * Signals that the class is anotated with {@link ProvidesSingleton}.
   * @return true iff the class is annotated as a producer class whose instances will produce a singleton.
   */
  boolean hasProducesSingletonAnnotation();

  /**
   * Signals that the instances of this class need to be injected after creation.
   * i.e : they have directly, or indirectly via a super class, {@link Inject} annotated
   * members.
   * In this case, they will be injected using the scope/injector used to create them.
   * @return true iff the instances of the class need to be injected.
   * @see Injector#inject(Object)
   */
  boolean needsInjection();
}
