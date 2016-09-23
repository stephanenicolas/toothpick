package toothpick;

/**
 * A non thread safe internal provider. It should never be exposed outside of Toothpick.
 *
 * @param <T> the class of the instances provided by this provider.
 */
public class ScopedProviderImpl<T> extends InternalProviderImpl<T> {
  protected Scope scope;

  public ScopedProviderImpl(Scope scope, Factory<?> factory, boolean isProviderFactory) {
    super(factory,
        isProviderFactory);
    this.scope = scope;
  }

  public ScopedProviderImpl(Scope scope, Class<?> factoryKeyClass,
      boolean isProviderFactoryClass,
      boolean isCreatingSingletonInScope,
      boolean isProvidingSingletonInScope) {
    super(factoryKeyClass,
        isProviderFactoryClass,
        isCreatingSingletonInScope,
        isProvidingSingletonInScope);
    this.scope = scope;
  }

  //we lock on the unbound provider itself to prevent concurrent usage
  //of the unbound provider (
  public T get(Scope scope) {
    return super.get(this.scope);
  }
}