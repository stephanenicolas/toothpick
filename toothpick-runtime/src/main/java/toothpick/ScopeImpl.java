package toothpick;

import toothpick.config.Binding;
import toothpick.config.Module;
import toothpick.configuration.ConfigurationHolder;
import toothpick.configuration.IllegalBindingException;
import toothpick.registries.FactoryRegistryLocator;

import javax.inject.Provider;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.Map;

import static java.lang.String.format;

/**
 * {@inheritDoc}
 * <p>
 * A note on concurrency :
 * <ul>
 * <li> all operations related to the scope tree are synchronized on the {@code Toothpick} class.
 * <li> all operations related to a scope's content (binding & providers) are synchronized on the key (class) of the binding/injection.
 * <li> all providers provided by the public API (including Lazy) should return a thread safe provider (done)
 * but internally, we can live with a non synchronized provider.
 * </ul>
 * <em>All operations on the scope itself are non thread-safe. They <em>must</em> be used via the {@code Toothpick} class
 * or <em>must</em> be synchronized using the {@code Toothpick} class if used concurrently.</em>
 * </p>
 */
public class ScopeImpl extends ScopeNode {
  private static final String LINE_SEPARATOR = System.getProperty("line.separator");

  private static IdentityHashMap<Class, InternalProviderImpl> mapClassesToUnNamedUnBoundProviders = new IdentityHashMap<>();
  private IdentityHashMap<Class, Map<String, InternalProviderImpl>> mapClassesToNamedBoundProviders = new IdentityHashMap<>();
  private IdentityHashMap<Class, InternalProviderImpl> mapClassesToUnNamedBoundProviders = new IdentityHashMap<>();
  private boolean hasTestModules;

  public ScopeImpl(Object name) {
    super(name);
    installBindingForScope();
  }

  @Override
  public <T> T getInstance(Class<T> clazz) {
    return getInstance(clazz, null);
  }

  @Override
  public <T> T getInstance(Class<T> clazz, String name) {
    crashIfClosed();
    ConfigurationHolder.configuration.checkCyclesStart(clazz, name);
    T t;
    try {
      t = lookupProvider(clazz, name).get(this);
    } finally {
      ConfigurationHolder.configuration.checkCyclesEnd(clazz, name);
    }
    return t;
  }

  @Override
  public <T> Provider<T> getProvider(Class<T> clazz) {
    return getProvider(clazz, null);
  }

  @Override
  public <T> Provider<T> getProvider(Class<T> clazz, String name) {
    crashIfClosed();
    return new ThreadSafeProviderImpl<>(this, clazz, name, false);
  }

  @Override
  public <T> Lazy<T> getLazy(Class<T> clazz) {
    return getLazy(clazz, null);
  }

  @Override
  public <T> Lazy<T> getLazy(Class<T> clazz, String name) {
    crashIfClosed();
    return new ThreadSafeProviderImpl<>(this, clazz, name, true);
  }

  @Override
  public synchronized void installTestModules(Module... modules) {
    if (hasTestModules) {
      throw new IllegalStateException("TestModules can only be installed once per scope.");
    }
    installModules(true, modules);
    hasTestModules = true;
  }

  @Override
  public void installModules(Module... modules) {
    installModules(false, modules);
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
    builder.append(LINE_SEPARATOR);

    builder.append("Providers: [");
    ArrayList<Class> sortedBoundProviderClassesList;
    synchronized (mapClassesToNamedBoundProviders) {
      sortedBoundProviderClassesList = new ArrayList(mapClassesToNamedBoundProviders.keySet());
    }
    synchronized (mapClassesToUnNamedBoundProviders) {
      sortedBoundProviderClassesList.addAll(mapClassesToUnNamedBoundProviders.keySet());
    }
    Collections.sort(sortedBoundProviderClassesList, new ClassNameComparator());
    for (Class aClass : sortedBoundProviderClassesList) {
      builder.append(aClass.getName());
      builder.append(',');
    }

    if (!sortedBoundProviderClassesList.isEmpty()) {
      builder.deleteCharAt(builder.length() - 1);
    }

    builder.append(']');
    builder.append(LINE_SEPARATOR);

    Iterator<ScopeNode> iterator = childrenScopes.values().iterator();
    while (iterator.hasNext()) {
      Scope scope = iterator.next();
      boolean isLast = !iterator.hasNext();
      builder.append(isLast ? lastNode : node);
      builder.append(branch);
      String childString = scope.toString();
      String[] split = childString.split(LINE_SEPARATOR);
      for (int i = 0; i < split.length; i++) {
        String childLine = split[i];
        if (i != 0) {
          builder.append(indent);
        }
        builder.append(childLine);
        builder.append(LINE_SEPARATOR);
      }
    }

    if (getRootScope() == this) {
      builder.append("Unbound providers: [");
      ArrayList<Class> sortedUnboundProviderClassesList;
      synchronized (mapClassesToUnNamedUnBoundProviders) {
        sortedUnboundProviderClassesList = new ArrayList(mapClassesToUnNamedUnBoundProviders.keySet());
      }
      Collections.sort(sortedUnboundProviderClassesList, new ClassNameComparator());

      for (Class aClass : sortedUnboundProviderClassesList) {
        builder.append(aClass.getName());
        builder.append(',');
      }
      if (!sortedUnboundProviderClassesList.isEmpty()) {
        builder.deleteCharAt(builder.length() - 1);
      }
      builder.append(']');
      builder.append(LINE_SEPARATOR);
    }

    return builder.toString();
  }

  private void installModules(boolean isTestModule, Module... modules) {
    for (Module module : modules) {
      try {
        installModule(isTestModule, module);
      } catch (Exception e) {
        throw new IllegalStateException(format("Module %s couldn't be installed", module.getClass().getName()), e);
      }
    }
  }

  private void installModule(boolean isTestModule, Module module) {
    for (Binding binding : module.getBindingSet()) {
      if (binding == null) {
        throw new IllegalStateException("A module can't have a null binding : " + module);
      }

      Class clazz = binding.getKey();
      String bindingName = binding.getName();
      try {
        if (isTestModule || getBoundProvider(clazz, bindingName) == null) {
          InternalProviderImpl provider = toProvider(binding);
          if (binding.isCreatingInstancesInScope()) {
            installScopedProvider(clazz, bindingName, (ScopedProviderImpl) provider, isTestModule);
          } else {
            installBoundProvider(clazz, bindingName, provider, isTestModule);
          }
        }
      } catch (Exception e) {
        throw new IllegalBindingException(format("Binding %s couldn't be installed", bindingName), e);
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
    ConfigurationHolder.configuration.checkIllegalBinding(binding, this);

    switch (binding.getMode()) {
      case SIMPLE:
        return createInternalProvider(this,
            binding.getKey(),
            false,
            binding.isCreatingInstancesInScope(),
            binding.isCreatingSingletonInScope(),
            false);
      case CLASS:
        return createInternalProvider(this,
            binding.getImplementationClass(),
            false,
            binding.isCreatingInstancesInScope(),
            binding.isCreatingSingletonInScope(),
            false);
      case INSTANCE:
        return new InternalProviderImpl<>(binding.getInstance());
      case PROVIDER_INSTANCE:
        // to ensure providers do not have to deal with concurrency, we wrap them in a thread safe provider
        // We do not need to pass the scope here because the provider won't use any scope to create the instance
        return new InternalProviderImpl<>(binding.getProviderInstance(), binding.isProvidingSingletonInScope());
      case PROVIDER_CLASS:
        return createInternalProvider(this,
            binding.getProviderClass(),
            true,
            binding.isCreatingInstancesInScope(),
            binding.isCreatingSingletonInScope(),
            binding.isProvidingSingletonInScope());
      //JACOCO:OFF
      default:
        throw new IllegalStateException(format("mode is not handled: %s. This should not happen.", binding.getMode()));
        //JACOCO:ON
    }
  }

  private <T> InternalProviderImpl<T> createInternalProvider(Scope scope, Class<?> factoryKeyClass,
      boolean isProviderClass,
      boolean isCreatingInstancesInScope,
      boolean isCreatingSingletonInScope,
      boolean isProvidingInstancesInScope) {
    if (isCreatingInstancesInScope) {
      return new ScopedProviderImpl<>(scope,
          factoryKeyClass,
          isProviderClass,
          isCreatingSingletonInScope,
          isProvidingInstancesInScope);
    } else {
      return new InternalProviderImpl<>(factoryKeyClass,
          isProviderClass,
          isCreatingSingletonInScope,
          isProvidingInstancesInScope);
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
  /* @VisibleForTesting */ <T> InternalProviderImpl<? extends T> lookupProvider(Class<T> clazz, String bindingName) {
    if (clazz == null) {
      throw new IllegalArgumentException("TP can't get an instance of a null class.");
    }
    InternalProviderImpl<? extends T> scopedProvider = getBoundProvider(clazz, bindingName);
    if (scopedProvider != null) {
      return scopedProvider;
    }
    Iterator<ScopeNode> iterator = parentScopes.iterator();
    while (iterator.hasNext()) {
      Scope parentScope = iterator.next();
      ScopeImpl parentScopeImpl = (ScopeImpl) parentScope;
      InternalProviderImpl<? extends T> parentScopedProvider = parentScopeImpl.getBoundProvider(clazz, bindingName);
      if (parentScopedProvider != null) {
        return parentScopedProvider;
      }
    }

    //if the binding is named
    //we couldn't find it in any scope, we must fail
    //as only unnamed bindings can be created dynamically
    if (bindingName != null) {
      throw new RuntimeException(format("No binding was defined for class %s and name %s " //
          + "in scope %s and its parents %s", clazz.getName(), bindingName, getName(), getParentScopesNames()));
    }

    //we now look for an unnamed binding
    //bindingName = null;  <-- valid but fails checkstyle as we use null directly

    //check if we have a cached un-scoped provider
    InternalProviderImpl unScopedProviderInPool = getUnBoundProvider(clazz, null);
    if (unScopedProviderInPool != null) {
      return unScopedProviderInPool;
    }

    //classes discovered at runtime, not bound by any module
    //they will be a bit slower as we need to get the factory first
    //we need to know whether they are scoped or not, if so we scope them
    //if not, they are place in the pool
    Factory<T> factory = FactoryRegistryLocator.getFactory(clazz);

    if (factory.hasScopeAnnotation()) {
      //the new provider will have to work in the current scope
      Scope targetScope = factory.getTargetScope(this);
      ScopedProviderImpl<? extends T> newProvider = new ScopedProviderImpl<>(targetScope, factory, false);
      //it is bound to its target scope only if it has a scope annotation.
      //lock free installing a provider means there could have been one set concurrently since last testing
      //its value. We allow to return it here
      ScopeImpl targetScopeImpl = (ScopeImpl) targetScope;
      return targetScopeImpl.installScopedProvider(clazz, null, newProvider, false);
    } else {
      //the provider is but in a pool of unbound providers for later reuse
      final InternalProviderImpl<T> newProvider = new InternalProviderImpl<>(factory,
          false);
      //the pool is static as it is accessible from all scopes
      //lock free installing a provider means there could have been one set concurrently since last testing
      //its value. We allow to return it here
      return installUnBoundProvider(clazz, null, newProvider);
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
    if (bindingName == null) {
      if (isBound) {
        synchronized (mapClassesToUnNamedBoundProviders) {
          return mapClassesToUnNamedBoundProviders.get(clazz);
        }
      } else {
        synchronized (mapClassesToUnNamedUnBoundProviders) {
          return mapClassesToUnNamedUnBoundProviders.get(clazz);
        }
      }
    } else {
      synchronized (mapClassesToNamedBoundProviders) {
        Map<String, InternalProviderImpl> mapNameToProvider = mapClassesToNamedBoundProviders.get(clazz);
        if (mapNameToProvider == null) {
          return null;
        }
        return mapNameToProvider.get(bindingName);
      }
    }
  }

  /**
   * Install the provider of the class {@code clazz} and name {@code bindingName}
   * in the current scope.
   *
   * @param clazz the class for which to install the scoped provider of this scope.
   * @param bindingName the name, possibly {@code null}, for which to install the scoped provider.
   * @param scopedProvider the internal provider to install.
   * @param isTestProvider whether or not is a test provider, installed through a Test Module that should override
   *                       existing providers for the same class-bindingname.
   * @param <T> the type of {@code clazz}.
   * @return the provider that will be installed, if one was previously installed, it is returned, in a lock-free way.
   */
  private <T> InternalProviderImpl<? extends T> installScopedProvider(Class<T> clazz, String bindingName,
      ScopedProviderImpl<? extends T> scopedProvider, boolean isTestProvider) {
    return installBoundProvider(clazz, bindingName, scopedProvider, isTestProvider);
  }

  /**
   * Install the provider of the class {@code clazz} and name {@code bindingName}
   * in the current scope.
   *
   * @param clazz the class for which to install the scoped provider.
   * @param bindingName the name, possibly {@code null}, for which to install the scoped provider.
   * @param internalProvider the internal provider to install.
   * @param isTestProvider whether or not is a test provider, installed through a Test Module that should override
   *                       existing providers for the same class-bindingname.
   * @param <T> the type of {@code clazz}.
   */
  private <T> InternalProviderImpl<? extends T> installBoundProvider(Class<T> clazz, String bindingName,
      InternalProviderImpl<? extends T> internalProvider, boolean isTestProvider) {
    return installInternalProvider(clazz, bindingName, internalProvider, true, isTestProvider);
  }

  /**
   * Install the provider of the class {@code clazz} and name {@code bindingName}
   * in the pool of unbound providers.
   *
   * @param clazz the class for which to install the provider.
   * @param bindingName the name, possibly {@code null}, for which to install the scoped provider.
   * @param internalProvider the internal provider to install.
   * @param <T> the type of {@code clazz}.
   */
  private <T> InternalProviderImpl<? extends T> installUnBoundProvider(Class<T> clazz, String bindingName,
      InternalProviderImpl<? extends T> internalProvider) {
    return installInternalProvider(clazz, bindingName, internalProvider, false, false);
  }

  /**
   * Installs a provider either in the scope or the pool of unbound providers.
   *
   * @param clazz the class for which to install the provider.
   * @param bindingName the name, possibly {@code null}, for which to install the scoped provider.
   * @param internalProvider the internal provider to install.
   * @param isBound whether or not the provider is bound to the scope or belongs to the pool of unbound providers.
   * @param isTestProvider whether or not is a test provider, installed through a Test Module that should override
   *                       existing providers for the same class-bindingname.
   * @param <T> the type of {@code clazz}.
   *
   * Note to maintainers : we don't use this method directly, both {@link #installBoundProvider(Class, String, InternalProviderImpl, boolean)}
   * and {@link #installUnBoundProvider(Class, String, InternalProviderImpl)}
   * are a facade of this method and make the calls more clear.
   */
  private <T> InternalProviderImpl installInternalProvider(Class<T> clazz, String bindingName, InternalProviderImpl<? extends T> internalProvider,
      boolean isBound, boolean isTestProvider) {
    if (bindingName == null) {
      if (isBound) {
        return installUnNamedProvider(mapClassesToUnNamedBoundProviders, clazz, internalProvider, isTestProvider);
      } else {
        return installUnNamedProvider(mapClassesToUnNamedUnBoundProviders, clazz, internalProvider, isTestProvider);
      }
    } else {
      return installNamedProvider(mapClassesToNamedBoundProviders, clazz, bindingName, internalProvider, isTestProvider);
    }
  }

  private <T> InternalProviderImpl installNamedProvider(IdentityHashMap<Class, Map<String, InternalProviderImpl>> mapClassesToNamedBoundProviders,
      Class<T> clazz, String bindingName, InternalProviderImpl<? extends T> internalProvider, boolean isTestProvider) {
    synchronized (mapClassesToNamedBoundProviders) {
      Map<String, InternalProviderImpl> mapNameToProvider = mapClassesToNamedBoundProviders.get(clazz);
      if (mapNameToProvider == null) {
        mapNameToProvider = new HashMap<>(1);
        mapClassesToNamedBoundProviders.put(clazz, mapNameToProvider);
        mapNameToProvider.put(bindingName, internalProvider);
        return internalProvider;
      }

      InternalProviderImpl previous = mapNameToProvider.get(bindingName);
      if (previous == null || isTestProvider) {
        mapNameToProvider.put(bindingName, internalProvider);
        return internalProvider;
      } else {
        return previous;
      }
    }
  }

  private <T> InternalProviderImpl installUnNamedProvider(IdentityHashMap<Class, InternalProviderImpl> mapClassesToUnNamedProviders, Class<T> clazz,
      InternalProviderImpl<? extends T> internalProvider, boolean isTestProvider) {
    synchronized (mapClassesToUnNamedProviders) {
      InternalProviderImpl previous = mapClassesToUnNamedProviders.get(clazz);
      if (previous == null || isTestProvider) {
        mapClassesToUnNamedProviders.put(clazz, internalProvider);
        return internalProvider;
      } else {
        return previous;
      }
    }
  }

  private void crashIfClosed() {
    if (!isOpen) {
      throw new IllegalStateException(String.format("The scope with name %s has been already closed."
          + " It can't be used to create new instances.", name));
    }
  }

  static void resetUnBoundProviders() {
    mapClassesToUnNamedUnBoundProviders.clear();
  }

  /**
   * Resets the state of the scope.
   * Useful for automation testing when we want to reset the scope used to install test modules.
   */
  @Override
  protected void reset() {
    super.reset();
    mapClassesToNamedBoundProviders.clear();
    mapClassesToUnNamedBoundProviders.clear();
    hasTestModules = false;
    installBindingForScope();
  }

  /**
   * Install bindings for scope.
   */
  private void installBindingForScope() {
    //it's always possible to get access to the scope that contains an injected object.
    installBoundProvider(Scope.class, null, new InternalProviderImpl<>(this), false);
  }

  private static class ClassNameComparator implements Comparator<Class> {
    @Override
    public int compare(Class o1, Class o2) {
      return o1.getName().compareTo(o2.getName());
    }
  }
}
