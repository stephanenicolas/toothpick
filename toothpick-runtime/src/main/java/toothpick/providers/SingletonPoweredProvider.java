package toothpick.providers;

import javax.inject.Provider;

/**
 * A provider that always return the same {@link javax.inject.Singleton} instance
 * of {@code T}.
 * @param <T> the type of the instances provided by this provider.
 */
public class SingletonPoweredProvider<T> implements Provider<T> {
  private T instance;

  public SingletonPoweredProvider(T instance) {
    this.instance = instance;
  }

  @Override public T get() {
    return instance;
  }
}
