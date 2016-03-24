package toothpick;

/**
 * Creates instance of classes.
 */
public interface MemberInjector<T> {
  void inject(T t, Injector injector);
}
