package toothpick;

import java.util.concurrent.Callable;
import javax.inject.Provider;

class ProviderCallable<T> extends ProviderImpl<T> implements Callable<T> {

  public ProviderCallable(Provider<T> provider) {
    super(provider, false);
  }

  @Override public T call() throws Exception {
    return get();
  }
}
