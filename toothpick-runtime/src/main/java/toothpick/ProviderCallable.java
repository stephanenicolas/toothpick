package toothpick;

import java.util.concurrent.Callable;
import javax.inject.Provider;

class ProviderCallable<T> implements Callable<T> {
  private final Provider<T> provider;

  public ProviderCallable(Provider<T> provider) {
    this.provider = provider;
  }

  @Override public T call() throws Exception {
    return provider.get();
  }
}
