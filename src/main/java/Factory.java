/**
 * Creates instance of classes.
 */
public interface Factory<T> {
  T createInstance(Injector injector);
}
