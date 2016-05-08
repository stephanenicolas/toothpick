package toothpick;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
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
  private static IdentityHashMap<Class, UnNamedAndNamedProviders> mapClassesToUnBoundProviders = new IdentityHashMap<>();
  protected IdentityHashMap<Class, UnNamedAndNamedProviders> mapClassesToAllProviders = new IdentityHashMap<>();
  private boolean hasTestModules;

  public ScopeImpl(Object name) {
    super(name);
    //it's always possible to get access to the scope that conitains an injected object.
    installBoundProvider(Scope.class, null, new InternalProviderImpl<>(this));
  }

  @Override
  public <T> T getInstance(Class<T> clazz) {
    Configuration.instance.checkCyclesStart(clazz);
    T t = lookupProvider(clazz, null).get(this);
    Configuration.instance.checkCyclesEnd(clazz);
    return t;
  }

  @Override
  public <T> T getInstance(Class<T> clazz, String name) {
    Configuration.instance.checkCyclesStart(clazz);
    T t = lookupProvider(clazz, name).get(this);
    Configuration.instance.checkCyclesEnd(clazz);
    return t;
  }

  @Override
  public <T> Provider<T> getProvider(Class<T> clazz) {
    InternalProviderImpl<? extends T> provider = lookupProvider(clazz, null);
    return new ThreadSafeProviderImpl<>(this, provider, false);
  }

  @Override
  public <T> Provider<T> getProvider(Class<T> clazz, String name) {
    InternalProviderImpl<? extends T> provider = lookupProvider(clazz, name);
    return new ThreadSafeProviderImpl<>(this, provider, false);
  }

  @Override
  public <T> Lazy<T> getLazy(Class<T> clazz) {
    InternalProviderImpl<? extends T> provider = lookupProvider(clazz, null);
    return new ThreadSafeProviderImpl<>(this, provider, true);
  }

  @Override
  public <T> Lazy<T> getLazy(Class<T> clazz, String name) {
    InternalProviderImpl<? extends T> provider = lookupProvider(clazz, name);
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
    ArrayList<Class> scopedProviders = new ArrayList(mapClassesToAllProviders.keySet());
    Collections.sort(scopedProviders, new ClassNameComparator());
    for (Class aClass : scopedProviders) {
      builder.append(aClass.getName());
      builder.append(',');
    } if (!mapClassesToAllProviders.isEmpty()) {
      builder.deleteCharAt(builder.length() - 1);
    }
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

    if (getRootScope() == this) {
      builder.append("UnScoped providers: [");
      ArrayList<Class> unscopedProviders = new ArrayList(mapClassesToUnBoundProviders.keySet());
      Collections.sort(unscopedProviders, new ClassNameComparator());
      for (Class aClass : unscopedProviders) {
        builder.append(aClass.getName());
        builder.append(',');
      }
      if (!mapClassesToUnBoundProviders.isEmpty()) {
        builder.deleteCharAt(builder.length() - 1);
      }
      builder.append(']');
      builder.append('\n');
    }

    return builder.toString();
  }

  private void installModule(Module module) {
    for (Binding binding : module.getBindingSet()) {
      if (binding == null) {
        throw new IllegalStateException("A module can't have a null binding.");
      }

      Class clazz = binding.getKey();
      synchronized (clazz) {
        String bindingName = binding.getName();
        if (!hasTestModules || getBoundProvider(clazz, bindingName) == null) {
          InternalProviderImpl provider = toProvider(binding);
          if (binding.isScoped()) {
            installScopedProvider(clazz, bindingName, (ScopedProviderImpl) provider);
          } else {
            installBoundProvider(clazz, bindingName, provider);
          }
        }
      }
    }
  }

  //do not change the return type to Provider<? extends T>.
  //it would be cool and more convenient for bindings, but it would
  //make the APIs very unstable as you could not get any instance of the
  //implementation class via an scope, it would fail but be syntactically valid.
  //only creating an instance of the interface is valid with this syntax.
  /*VisibleForTesting*/ <T> InternalProviderImpl<T> toProvider(Binding<T> binding) {
    if (binding == null) {
      throw new IllegalStateException("null binding are not allowed. Should not happen unless getBindingSet is overridden.");
    }
    Configuration.instance.checkIllegalBinding(binding);

    switch (binding.getMode()) {
      case SIMPLE:
        return createInternalProvider(this, binding.getKey(), false, binding.isScoped());
      case CLASS:
        return createInternalProvider(this, binding.getImplementationClass(), false, binding.isScoped());
      case INSTANCE:
        return new InternalProviderImpl<>(binding.getInstance());
      case PROVIDER_INSTANCE:
        // to ensure providers do not have to deal with concurrency, we wrap them in a thread safe provider
        // We do not need to pass the scope here because the provider won't use any scope to create the instance
        return new InternalProviderImpl<>(binding.getProviderInstance(), false);
      case PROVIDER_CLASS:
        return createInternalProvider(this, binding.getProviderClass(), true, binding.isScoped());
      //JACOCO:OFF
      default:
        throw new IllegalStateException(format("mode is not handled: %s. This should not happen.", binding.getMode()));
        //JACOCO:ON
    }
  }

  private <T> InternalProviderImpl<T> createInternalProvider(Scope scope, Class<?> factoryKeyClass, boolean isProviderClass, boolean isScoped) {
    if (isScoped) {
      return new ScopedProviderImpl<>(scope, factoryKeyClass, isProviderClass);
    } else {
      return new InternalProviderImpl<>(factoryKeyClass, isProviderClass);
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
   * @param clazz the {@link Class} of {@code T} for which we lookup an {@link InternalProviderImpl}.
   * @param bindingName the potential name of the provider when it was bound (which means we always returned a scoped provider if
   * name is not null).
   * @param <T> the type for which we lookup an {@link InternalProviderImpl}.
   * @return a provider associated to the {@code T}. The returned provider is un-scoped (remember that {@link ScopedProviderImpl} is a subclass of
   * {@link InternalProviderImpl}). The returned provider will be scoped by the public methods to use the current scope.
   */
  private <T> InternalProviderImpl<? extends T> lookupProvider(Class<T> clazz, String bindingName) {
    if (clazz == null) {
      throw new IllegalArgumentException("TP can't get an instance of a null class.");
    }
    synchronized (clazz) {
      InternalProviderImpl<? extends T> scopedProvider = getBoundProvider(clazz, bindingName);
      if (scopedProvider != null) {
        return scopedProvider;
      }
      Iterator<Scope> iterator = parentScopes.iterator();
      while (iterator.hasNext()) {
        Scope parentScope = iterator.next();
        ScopeImpl parentScopeImpl = (ScopeImpl) parentScope;
        InternalProviderImpl<? extends T> parentScopedProvider = parentScopeImpl.getBoundProvider(clazz, bindingName);
        if (parentScopedProvider != null) {
          return parentScopedProvider;
        }
      }

      //check if we have a cached un-scoped provider
      InternalProviderImpl unScopedProviderInPool = getUnBoundProvider(clazz, bindingName);
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
        targetScopeImpl.installScopedProvider(clazz, bindingName, newProvider);
        return newProvider;
      } else {
        //the provider is but in a pool of unbound providers for later reuse
        final InternalProviderImpl<T> newProvider = new InternalProviderImpl<>(factory, false);
        //the pool is static as it is accessible from all scopes
        installUnBoundProvider(clazz, bindingName, newProvider);
        return newProvider;
      }
    }
  }

  /**
   * Obtains the provider of the class {@code clazz} and name {@code bindingName}, if any. The returned provider
   * will be bound to the scope. It can be {@code null} if there is no such provider.
   * Ancestors are not taken into account.
   *
   * @param clazz the class for which to obtain the bound provider.
   * @param bindingName the name, possibly {@code null}, for which to obtain the bound provider.
   * @param <T> the type of {@code clazz}.
   * @return the bound provider for class {@code clazz} and {@code bindingName}. Returns {@code null} is there
   * is no such bound provider.
   */
  private <T> InternalProviderImpl<? extends T> getBoundProvider(Class<T> clazz, String bindingName) {
    return getInternalProvider(clazz, bindingName, true);
  }

  /**
   * Obtains the provider of the class {@code clazz} and name {@code bindingName}, if any. The returned provider
   * will belong to the pool of unbound providers. It can be {@code null} if there is no such provider.
   *
   * @param clazz the class for which to obtain the unbound provider.
   * @param bindingName the name, possibly {@code null}, for which to obtain the unbound provider.
   * @param <T> the type of {@code clazz}.
   * @return the unbound provider for class {@code clazz} and {@code bindingName}. Returns {@code null} is there
   * is no such unbound provider.
   */
  private <T> InternalProviderImpl<? extends T> getUnBoundProvider(Class<T> clazz, String bindingName) {
    return getInternalProvider(clazz, bindingName, false);
  }

  /**
   * Obtains the provider of the class {@code clazz} and name {@code bindingName}. The returned provider
   * can either be bound to the scope or not depending on {@code isBound}.
   * Ancestors are not taken into account.
   *
   * @param clazz the class for which to obtain the provider.
   * @param bindingName the name, possibly {@code null}, for which to obtain the provider.
   * @param <T> the type of {@code clazz}.
   * @return the provider for class {@code clazz} and {@code bindingName},
   * either from the set of providers bound to the scope or from the pool of unbound providers.
   * If there is no such provider, returns {@code null}.
   *
   * Note to maintainers : we don't use this method directly, both {@link #getBoundProvider} and {@link #getUnBoundProvider}
   * are a facade of this method and make the calls more clear.
   */
  private <T> InternalProviderImpl<? extends T> getInternalProvider(Class<T> clazz, String bindingName, boolean isBound) {
    Map<Class, UnNamedAndNamedProviders> map;
    if (isBound) {
      map = mapClassesToAllProviders;
    } else {
      map = mapClassesToUnBoundProviders;
    }

    synchronized (clazz) {
      UnNamedAndNamedProviders<T> unNamedAndNamedProviders = map.get(clazz);
      if (unNamedAndNamedProviders == null) {
        return null;
      }
      if (bindingName == null) {
        return unNamedAndNamedProviders.unNamedProvider;
      }

      Map<String, InternalProviderImpl<? extends T>> mapNameToProvider = unNamedAndNamedProviders.getMapNameToProvider();
      if (mapNameToProvider == null) {
        return null;
      }
      return mapNameToProvider.get(bindingName);
    }
  }

  /**
   * Install the provider of the class {@code clazz} and name {@code bindingName}
   * in the current scope.
   *
   * @param clazz the class for which to install the scoped provider of this scope.
   * @param bindingName the name, possibly {@code null}, for which to install the scoped provider.
   * @param <T> the type of {@code clazz}.
   */
  private <T> void installScopedProvider(Class<T> clazz, String bindingName, ScopedProviderImpl<? extends T> scopedProvider) {
    installBoundProvider(clazz, bindingName, scopedProvider);
  }

  /**
   * Install the provider of the class {@code clazz} and name {@code bindingName}
   * in the current scope.
   *
   * @param clazz the class for which to install the scoped provider.
   * @param bindingName the name, possibly {@code null}, for which to install the scoped provider.
   * @param <T> the type of {@code clazz}.
   */
  private <T> void installBoundProvider(Class<T> clazz, String bindingName, InternalProviderImpl<? extends T> internalProvider) {
    installInternalProvider(clazz, bindingName, internalProvider, true);
  }

  /**
   * Install the provider of the class {@code clazz} and name {@code bindingName}
   * in the pool of unbound providers.
   *
   * @param clazz the class for which to install the provider.
   * @param bindingName the name, possibly {@code null}, for which to install the scoped provider.
   * @param <T> the type of {@code clazz}.
   */
  private <T> void installUnBoundProvider(Class<T> clazz, String bindingName, InternalProviderImpl<? extends T> internalProvider) {
    installInternalProvider(clazz, bindingName, internalProvider, false);
  }

  /**
   * Installs a provider either in the scope or the pool of unbound providers.
   *
   * @param clazz the class for which to install the provider.
   * @param bindingName the name, possibly {@code null}, for which to install the scoped provider.
   * @param internalProvider the internal provider to install.
   * @param isBound whether or not the provider is bound to the scope or belongs to the pool of unbound providers
   * @param <T> the type of {@code clazz}.
   *
   * Note to maintainers : we don't use this method directly, both {@link #installBoundProvider(Class, String, InternalProviderImpl)}
   * and {@link #installUnBoundProvider(Class, String, InternalProviderImpl)}
   * are a facade of this method and make the calls more clear.
   */
  private <T> void installInternalProvider(Class<T> clazz, String bindingName, InternalProviderImpl<? extends T> internalProvider, boolean isBound) {
    Map<Class, UnNamedAndNamedProviders> map;
    if (isBound) {
      map = mapClassesToAllProviders;
    } else {
      map = mapClassesToUnBoundProviders;
    }

    synchronized (clazz) {
      UnNamedAndNamedProviders<T> unNamedAndNamedProviders = map.get(clazz);
      if (unNamedAndNamedProviders == null) {
        unNamedAndNamedProviders = new UnNamedAndNamedProviders<>();
        map.put(clazz, unNamedAndNamedProviders);
      }
      if (bindingName == null) {
        unNamedAndNamedProviders.setUnNamedProvider(internalProvider);
      } else {
        Map<String, InternalProviderImpl<? extends T>> mapNameToProvider = unNamedAndNamedProviders.getMapNameToProvider();
        if (mapNameToProvider == null) {
          mapNameToProvider = new HashMap<>();
          unNamedAndNamedProviders.setMapNameToProvider(mapNameToProvider);
        }
        mapNameToProvider.put(bindingName, internalProvider);
      }
    }
  }

  private static class UnNamedAndNamedProviders<T> {
    private InternalProviderImpl<? extends T> unNamedProvider;
    private Map<String, InternalProviderImpl<? extends T>> mapNameToProvider;

    public Map<String, InternalProviderImpl<? extends T>> getMapNameToProvider() {
      return mapNameToProvider;
    }

    public void setMapNameToProvider(Map<String, InternalProviderImpl<? extends T>> mapNameToProvider) {
      this.mapNameToProvider = mapNameToProvider;
    }

    public void setUnNamedProvider(InternalProviderImpl<? extends T> unNamedProvider) {
      this.unNamedProvider = unNamedProvider;
    }
  }

  static void reset() {
    mapClassesToUnBoundProviders.clear();
  }

  private static class ClassNameComparator implements Comparator<Class> {
    @Override
    public int compare(Class o1, Class o2) {
      return o1.getName().compareTo(o2.getName());
    }
  }
}
