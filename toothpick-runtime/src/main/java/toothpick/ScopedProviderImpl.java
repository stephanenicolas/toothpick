package toothpick;

import javax.inject.Provider;

/**
 * A non thread safe internal provider. It should never be exposed outside of ToothPick.
 *
 * @param <T> the class of the instances provided by this provider.
 */
public class ScopedProviderImpl<T> extends UnScopedProviderImpl<T> implements Provider<T>, Lazy<T> {
  protected Scope scope;
  public ScopedProviderImpl(T instance) {
    super(instance);
  }

  public ScopedProviderImpl(Provider<? extends T> providerInstance, boolean isLazy) {
    super(providerInstance, isLazy);
  }

  public ScopedProviderImpl(Scope scope, Factory<?> factory, boolean isProviderFactory) {
    super(factory, isProviderFactory);
    this.scope = scope;
  }

  public ScopedProviderImpl(Scope scope, Class<?> factoryKeyClass, boolean isProviderFactoryClass) {
    super(factoryKeyClass, isProviderFactoryClass);
    this.scope = scope;
  }

  @Override
  public T get() {
    return super.get(scope);
  }

  //we lock on the unbound provider itself to prevent concurrent usage
  //of the unbound provider (
  public T get(Scope scope) {
    return get();
  }
}