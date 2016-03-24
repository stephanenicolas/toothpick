package toothpick;

/**
 * Provides instances of a given type.
 * This indirection layer to accessing instances
 * is the key of DI in toothpick. It is a class answer, in Uncle's Bob meaning,
 * to accessing DI managed instances.
 */
public interface Provider<T> {
  T get();

  void setInjector(Injector injector);
}
