package toothpick.providers;

public class SingletonPoweredProvider<T> extends BaseProvider<T> {
  private T instance;

  public SingletonPoweredProvider(T instance) {
    super(null);
    this.instance = instance;
  }

  @Override public T get() {
    return instance;
  }
}
