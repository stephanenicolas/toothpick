package toothpick;

import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.Map;
import javax.inject.Provider;
import toothpick.config.Binding;
import toothpick.config.Module;
import toothpick.registries.factory.FactoryRegistryLocator;

import static java.lang.String.format;

/**
 * {@inheritDoc}
 * <p>
 * A note on concurrency :
 * <ul>
 * <li> all operations related to the scope tree are synchronized on the {@code ToothPick} class.
 * <li> all operations related to a scope's content (binding & providers) are synchronized on the key (class) of the binding/injection.
 * <li> all providers provided by the public API (including Lazy) should return a thread safe provider (done)
 * but internally, we can live with a non synchronized provider.
 * </ul>
 * <em>All operations on the scope itself are non thread-safe. They <em>must</em> be used via the {@code ToothPick} class
 * or <em>must</em> be synchronized using the {@code ToothPick} class if used concurrently.</em>
 * </p>
 */
public class ScopeImpl extends Scope {
  private static IdentityHashMap<Class, UnScopedProviderImpl> mapClassesToUnScopedProviders = new IdentityHashMap<>();
  protected IdentityHashMap<Class, UnNamedAndNamedProviders> mapClassesToAllProviders = new IdentityHashMap<>();
  private boolean hasTestModules;

  public ScopeImpl(Object name) {
    super(name);
    //it's always possible to get access to the scope that contains an injected object.
    installScopedProvider(Scope.class, null, new ScopedProviderImpl<>(this));
  }

  @Override
  public <T> T getInstance(Class<T> clazz) {
    return getProviderInternal(clazz, null).get(this);
  }

  @Override
  public <T> T getInstance(Class<T> clazz, String name) {
    return getProviderInternal(clazz, name).get(this);
  }

  @Override
  public <T> Provider<T> getProvider(Class<T> clazz) {
    UnScopedProviderImpl<? extends T> provider = getProviderInternal(clazz, null);
    return new ThreadSafeProviderImpl<>(this, provider, false);
  }

  @Override
  public <T> Provider<T> getProvider(Class<T> clazz, String name) {
    UnScopedProviderImpl<? extends T> provider = getProviderInternal(clazz, name);
    return new ThreadSafeProviderImpl<>(this, provider, false);
  }

  @Override
  public <T> Lazy<T> getLazy(Class<T> clazz) {
    UnScopedProviderImpl<? extends T> provider = getProviderInternal(clazz, null);
    return new ThreadSafeProviderImpl<>(this, provider, true);
  }

  @Override
  public <T> Lazy<T> getLazy(Class<T> clazz, String name) {
    UnScopedProviderImpl<? extends T> provider = getProviderInternal(clazz, name);
    return new ThreadSafeProviderImpl<>(this, provider, true);
  }

  @Override
  public void installTestModules(Module... modules) {
    //we allow multiple calls to this method
    boolean oldHasTestModules = hasTestModules;
    hasTestModules = false;
    installModules(modules);
    boolean doOverrideModulesExist = modules != null;
    hasTestModules = oldHasTestModules || doOverrideModulesExist;
  }

  @Override
  public void installModules(Module... modules) {
    for (Module module : modules) {
      installModule(module);
    }
  }

  @Override
  public String toString() {
    final String branch = "---";
    final char lastNode = '\\';
    final char node = '+';
    final String indent = "    ";

    StringBuilder builder = new StringBuilder();
    builder.append(name);
    builder.append(':');
    builder.append(System.identityHashCode(this));
    builder.append('\n');

    builder.append("Providers: [");
    for (Class aClass : mapClassesToAllProviders.keySet()) {
      builder.append(aClass.getName());
      builder.append(',');
    }
    builder.deleteCharAt(builder.length() - 1);
    builder.append(']');
    builder.append('\n');

    Iterator<Scope> iterator = childrenScopes.iterator();
    while (iterator.hasNext()) {
      Scope scope = iterator.next();
      boolean isLast = !iterator.hasNext();
      builder.append(isLast ? lastNode : node);
      builder.append(branch);
      String childString = scope.toString();
      String[] split = childString.split("\n");
      for (int i = 0; i < split.length; i++) {
        String childLine = split[i];
        if (i != 0) {
          builder.append(indent);
        }
        builder.append(childLine);
        builder.append('\n');
      }
    }

    builder.append("UnScoped providers : [");
    for (Class aClass : mapClassesToUnScopedProviders.keySet()) {
      builder.append(aClass.getName());
      builder.append(',');
    }
    builder.deleteCharAt(builder.length() - 1);
    builder.append(']');
    builder.append('\n');

    return builder.toString();
  }

  private void installModule(Module module) {
    for (Binding binding : module.getBindingSet()) {
      if (binding == null) {
        throw new IllegalStateException("A module can't have a null binding : " + module);
      }

      Class clazz = binding.getKey();
      String bindingName = binding.getName();
      if (!hasTestModules || getScopedProvider(clazz, bindingName) == null) {
        ScopedProviderImpl provider = toProvider(binding);
        installScopedProvider(clazz, bindingName, provider);
      }
    }
  }

  //do not change the return type to Provider<? extends T>.
  //it would be cool and more convenient for bindings, but it would
  //make the APIs very unstable as you could not get any instance of the
  //implementation class via an scope, it would fail but be syntactically valid.
  //only creating an instance of the interface is valid with this syntax.
  /*VisibleForTesting*/ <T> ScopedProviderImpl<T> toProvider(Binding<T> binding) {
    if (binding == null) {
      throw new IllegalStateException("null binding are not allowed. Should not happen unless getBindingSet is overridden.");
    }
    switch (binding.getMode()) {
      case SIMPLE:
        return new ScopedProviderImpl<>(this, binding.getKey(), false);
      case CLASS:
        return new ScopedProviderImpl<>(this, binding.getImplementationClass(), false);
      case INSTANCE:
        return new ScopedProviderImpl<>(binding.getInstance());
      case PROVIDER_INSTANCE:
        //to ensure providers do not have to deal with concurrency, we wrap them in a thread safe provider
        return new ScopedProviderImpl<>(binding.getProviderInstance(), false);
      case PROVIDER_CLASS:
        return new ScopedProviderImpl<>(this, binding.getProviderClass(), true);

      //JACOCO:OFF
      default:
        throw new IllegalStateException(format("mode is not handled: %s. This should not happen.", binding.getMode()));
        //JACOCO:ON
    }
  }

  /**
   * The core of Toothpick internals : the provider lookup.
   * It will look for a scoped provider, bubbling up in the scope hierarchy.
   * If one is found, we return it. If not, we look in the un-scoped provider pool,
   * if one is found, we return it. If not, we create a provider dynamically, using a factory. Depending
   * on the whether or not the discovered factory for this class is scoped (={@link javax.inject.Scope} annotated),
   * the provider will be scoped or not. If it is scoped, it will be scoped in the appropriate scope, if not
   * it will be added to the pool of un-scoped providers.
   * Note that
   *
   * @param clazz the {@link Class} of {@code T} for which we lookup an {@link UnScopedProviderImpl}.
   * @param name the potential name of the provider when it was bound (which means we always returned a scoped provider if
   * name is not null).
   * @param <T> the type for which we lookup an {@link UnScopedProviderImpl}.
   * @return a provider associated to the {@code T}. The returned provider is un-scoped (remember that {@link ScopedProviderImpl} is a subclass of
   * {@link UnScopedProviderImpl}). The returned provider will be scoped by the public methods to use the current scope.
   */
  private <T> UnScopedProviderImpl<? extends T> getProviderInternal(Class<T> clazz, String name) {
    if (clazz == null) {
      throw new IllegalArgumentException("TP can't get an instance of a null class.");
    }
    synchronized (clazz) {
      ScopedProviderImpl<? extends T> scopedProvider = getScopedProvider(clazz, name);
      if (scopedProvider != null) {
        return scopedProvider;
      }
      Iterator<Scope> iterator = parentScopes.iterator();
      while (iterator.hasNext()) {
        Scope parentScope = iterator.next();
        ScopeImpl parentScopeImpl = (ScopeImpl) parentScope;
        ScopedProviderImpl<? extends T> parentScopedProvider = parentScopeImpl.getScopedProvider(clazz, name);
        if (parentScopedProvider != null) {
          return parentScopedProvider;
        }
      }

      //check if we have a cached un-scoped provider
      UnScopedProviderImpl unScopedProviderInPool = mapClassesToUnScopedProviders.get(clazz);
      if (unScopedProviderInPool != null) {
        return unScopedProviderInPool;
      }

      //classes discovered at runtime, not bound by any module
      //they will be a bit slower as we need to get the factory first
      //we need to know whether they are scoped or not, if so we scope them
      //if not, they are place in the pool
      Factory<T> factory = FactoryRegistryLocator.getFactory(clazz);

      Scope targetScope = factory.getTargetScope(this);
      ScopeImpl targetScopeImpl = (ScopeImpl) targetScope;
      if (factory.hasScopeAnnotation()) {
        //the new provider will have to work in the current scope
        final ScopedProviderImpl<T> newProvider = new ScopedProviderImpl<>(targetScope, factory, false);
        //it is bound to its target scope only if it has a scope annotation.
        targetScopeImpl.installScopedProvider(clazz, name, newProvider);
        return newProvider;
      } else {
        //the provider is but in a pool of unbound providers for later reuse
        final UnScopedProviderImpl<T> newProvider = new UnScopedProviderImpl<>(factory, false);
        //the pool is static as it is accessible from all scopes
        installUnScopedProvider(clazz, newProvider);
        return newProvider;
      }
    }
  }

  /**
   * Obtains the provider of the class {@code clazz} and name {@code bindingName}
   * that is scoped in the current scope, if any.
   * Ancestors are not taken into account.
   *
   * @param clazz the class for which to obtain the scoped provider of this scope, if one is scoped.
   * @param bindingName the name, possibly {@code null}, for which to obtain the scoped provider of this scope, if one is scoped.
   * @param <T> the type of {@code clazz}.
   * @return the scoped provider of this scope for class {@code clazz} and {@code bindingName},
   * if one is scoped, {@code null} otherwise.
   */
  private <T> ScopedProviderImpl<? extends T> getScopedProvider(Class<T> clazz, String bindingName) {
    UnNamedAndNamedProviders<T> unNamedAndNamedProviders = mapClassesToAllProviders.get(clazz);
    if (unNamedAndNamedProviders == null) {
      return null;
    }
    if (bindingName == null) {
      return unNamedAndNamedProviders.unNamedProvider;
    }

    Map<String, ScopedProviderImpl<? extends T>> mapNameToProvider = unNamedAndNamedProviders.getMapNameToProvider();
    if (mapNameToProvider == null) {
      return null;
    }
    return mapNameToProvider.get(bindingName);
  }

  /**
   * Install the provider of the class {@code clazz} and name {@code bindingName}
   * in the current scope.
   *
   * @param clazz the class for which to install the scoped provider of this scope.
   * @param bindingName the name, possibly {@code null}, for which to install the scoped provider.
   * @param <T> the type of {@code clazz}.
   */
  private <T> void installScopedProvider(Class<T> clazz, String bindingName, ScopedProviderImpl<? extends T> provider) {
    UnNamedAndNamedProviders<T> unNamedAndNamedProviders = mapClassesToAllProviders.get(clazz);
    if (unNamedAndNamedProviders == null) {
      unNamedAndNamedProviders = new UnNamedAndNamedProviders<>();
      try {
        Thread.sleep(0,500);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
      mapClassesToAllProviders.putIfAbsent(clazz, unNamedAndNamedProviders);
    }
    if (bindingName == null) {
      unNamedAndNamedProviders.setUnNamedProvider(provider);
    } else {
      Map<String, ScopedProviderImpl<? extends T>> mapNameToProvider = unNamedAndNamedProviders.getMapNameToProvider();
      if (mapNameToProvider == null) {
        mapNameToProvider = new HashMap<>();
        unNamedAndNamedProviders.setMapNameToProvider(mapNameToProvider);
      }
      mapNameToProvider.put(bindingName, provider);
    }
  }

  /**
   * Install the unScopedProvider of the class {@code clazz}
   * in the pool of unscoped providers.
   *
   * @param clazz the class for which to install the unscoped unScopedProvider.
   * @param <T> the type of {@code clazz}.
   */
  private <T> void installUnScopedProvider(Class<T> clazz, UnScopedProviderImpl<? extends T> unScopedProvider) {
    synchronized (clazz) {
      mapClassesToUnScopedProviders.put(clazz, unScopedProvider);
    }
  }

  private static class UnNamedAndNamedProviders<T> {
    private ScopedProviderImpl<? extends T> unNamedProvider;
    private Map<String, ScopedProviderImpl<? extends T>> mapNameToProvider;

    public Map<String, ScopedProviderImpl<? extends T>> getMapNameToProvider() {
      return mapNameToProvider;
    }

    public void setMapNameToProvider(Map<String, ScopedProviderImpl<? extends T>> mapNameToProvider) {
      this.mapNameToProvider = mapNameToProvider;
    }

    public void setUnNamedProvider(ScopedProviderImpl<? extends T> unNamedProvider) {
      this.unNamedProvider = unNamedProvider;
    }
  }
}
