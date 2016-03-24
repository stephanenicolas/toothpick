package toothpick;

/**
 * Creates instance of classes.
 */
public interface Factory<T> {
  T createInstance(Injector injector);
  boolean hasSingletonAnnotation();
  boolean hasProducesSingletonAnnotation();
}
