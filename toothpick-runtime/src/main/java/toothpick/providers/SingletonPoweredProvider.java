package toothpick.providers;

import toothpick.Provider;

public class SingletonPoweredProvider<T> implements Provider<T> {
  private T instance;

  public SingletonPoweredProvider(T instance) {
    this.instance = instance;
  }

  @Override public T get() {
    return instance;
  }
}
