package toothpick;

import javax.inject.Provider;

class LazyImpl<T> implements Lazy<T> {
  private final Provider<T> provider;
  private T instance;

  public LazyImpl(Provider<T> provider) {
    this.provider = provider;
  }

  @Override public T get() {
    if (instance == null) {
      instance = provider.get();
    }
    return instance;
  }
}
