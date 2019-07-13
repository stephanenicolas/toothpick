/*
 * Copyright 2016 Stephane Nicolas
 * Copyright 2016 Daniel Molinero Reguerra
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package toothpick;

import static java.lang.String.format;

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
import toothpick.configuration.ConfigurationHolder;
import toothpick.configuration.IllegalBindingException;
import toothpick.locators.FactoryLocator;

/**
 * {@inheritDoc}
 *
 * <p>A note on concurrency :
 *
 * <ul>
 *   <li>all operations related to the scope tree are synchronized on the {@code Toothpick} class.
 *   <li>all operations related to a scope's content (binding & providers) are synchronized on the
 *       key (class) of the binding/injection.
 *   <li>all providers provided by the public API (including Lazy) should return a thread safe
 *       provider (done) but internally, we can live with a non synchronized provider.
 * </ul>
 *
 * <em>All operations on the scope itself are non thread-safe. They <em>must</em> be used via the
 * {@code Toothpick} class or <em>must</em> be synchronized using the {@code Toothpick} class if
 * used concurrently.</em>
 */
public class ScopeImpl extends ScopeNode {
  private static final String LINE_SEPARATOR = System.getProperty("line.separator");

  /* This map is static and contains internal bindings / providers that will be
   * available to all scopes. The internal providers contained in the map will not be
   * scoped to a specific scope, so that any scope can be used to create instances from
   * this provider. The map will only contain the bindings that are created in the case
   * of dynamic discovery of non annotated factories.
   * We could have added a copy of the bindings to each scope, but it would have entailed
   * the recreation of the factory instance for each of them. Hence we use a static map
   * to keep the factory available to all scopes.
   */
  /*@VisibleForTesting */ static IdentityHashMap<Class, InternalProviderImpl>
      mapClassesToUnNamedUnScopedProviders = new IdentityHashMap<>();

  /*
   * These 2 maps contain the internal bindings / providers specific to a scope.
   */
  /*@VisibleForTesting */ final IdentityHashMap<Class, Map<String, InternalProviderImpl>>
      mapClassesToNamedScopedProviders = new IdentityHashMap<>();
  /*@VisibleForTesting */ final IdentityHashMap<Class, InternalProviderImpl>
      mapClassesToUnNamedScopedProviders = new IdentityHashMap<>();

  private boolean hasTestModules;

  public ScopeImpl(Object name) {
    super(name);
    installBindingForScopeClass();
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
  public synchronized Scope installTestModules(Module... modules) {
    if (hasTestModules) {
      throw new IllegalStateException("TestModules can only be installed once per scope.");
    }
    installModules(true, modules);
    hasTestModules = true;
    return this;
  }

  @Override
  public Scope installModules(Module... modules) {
    installModules(false, modules);
    return this;
  }

  @Override
  public void inject(Object obj) {
    Toothpick.inject(obj, this);
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
    ArrayList<Class> sortedScopedProviderClassesList;
    synchronized (mapClassesToNamedScopedProviders) {
      sortedScopedProviderClassesList = new ArrayList(mapClassesToNamedScopedProviders.keySet());
    }
    synchronized (mapClassesToUnNamedScopedProviders) {
      sortedScopedProviderClassesList.addAll(mapClassesToUnNamedScopedProviders.keySet());
    }
    Collections.sort(sortedScopedProviderClassesList, new ClassNameComparator());
    for (Class aClass : sortedScopedProviderClassesList) {
      builder.append(aClass.getName());
      builder.append(',');
    }

    if (!sortedScopedProviderClassesList.isEmpty()) {
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
      builder.append("UnScoped providers: [");
      ArrayList<Class> sortedUnScopedProviderClassesList;
      synchronized (mapClassesToUnNamedUnScopedProviders) {
        sortedUnScopedProviderClassesList =
            new ArrayList(mapClassesToUnNamedUnScopedProviders.keySet());
      }
      Collections.sort(sortedUnScopedProviderClassesList, new ClassNameComparator());

      for (Class aClass : sortedUnScopedProviderClassesList) {
        builder.append(aClass.getName());
        builder.append(',');
      }
      if (!sortedUnScopedProviderClassesList.isEmpty()) {
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
        throw new IllegalStateException(
            format("Module %s couldn't be installed", module.getClass().getName()), e);
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
        if (isTestModule || getScopedProvider(clazz, bindingName) == null) {
          InternalProviderImpl provider = toProvider(binding);
          installScopedProvider(clazz, bindingName, provider, isTestModule);
        }
      } catch (Exception e) {
        throw new IllegalBindingException(
            format("Binding %s couldn't be installed", bindingName), e);
      }
    }
  }

  // do not change the return type to Provider<? extends T>.
  // it would be cool and more convenient for bindings, but it would
  // make the APIs very unstable as you could not get any instance of the
  // implementation class via an scope, it would fail but be syntactically valid.
  // only creating an instance of the interface is valid with this syntax.
  /*VisibleForTesting*/ <T> InternalProviderImpl<T> toProvider(Binding<T> binding) {
    if (binding == null) {
      throw new IllegalStateException(
          "null binding are not allowed. Should not happen unless getBindingSet is overridden.");
    }
    ConfigurationHolder.configuration.checkIllegalBinding(binding, this);

    switch (binding.getMode()) {
      case SIMPLE:
        return createInternalProvider(
            binding.getKey(),
            false,
            binding.isCreatingSingleton(),
            binding.isCreatingReleasable(),
            false,
            false);
      case CLASS:
        return createInternalProvider(
            binding.getImplementationClass(),
            false,
            binding.isCreatingSingleton(),
            binding.isCreatingReleasable(),
            false,
            false);
      case INSTANCE:
        return new InternalProviderImpl<>(binding.getInstance());
      case PROVIDER_INSTANCE:
        // to ensure providers do not have to deal with concurrency, we wrap them in a thread safe
        // provider
        // We do not need to pass the scope here because the provider won't use any scope to create
        // the instance
        return new InternalProviderImpl<>(
            binding.getProviderInstance(),
            binding.isProvidingSingleton(),
            binding.isProvidingReleasable());
      case PROVIDER_CLASS:
        return createInternalProvider(
            binding.getProviderClass(),
            true,
            binding.isCreatingSingleton(),
            binding.isCreatingReleasable(),
            binding.isProvidingSingleton(),
            binding.isProvidingReleasable());
        // JACOCO:OFF
      default:
        throw new IllegalStateException(
            format("mode is not handled: %s. This should not happen.", binding.getMode()));
        // JACOCO:ON
    }
  }

  private <T> InternalProviderImpl<T> createInternalProvider(
      Class<?> factoryKeyClass,
      boolean isProviderClass,
      boolean isCreatingSingleton,
      boolean isCreatingReleasable,
      boolean isProvidingSingleton,
      boolean isProvidingReleasable) {
    return new ScopedProviderImpl<>(
        this,
        factoryKeyClass,
        isProviderClass,
        isCreatingSingleton,
        isCreatingReleasable,
        isProvidingSingleton,
        isProvidingReleasable);
  }

  /**
   * The core of Toothpick internals : the provider lookup. It will look for a scoped provider,
   * bubbling up in the scope hierarchy. If one is found, we return it. If not, we look in the
   * un-scoped provider pool, if one is found, we return it. If not, we create a provider
   * dynamically, using a factory. Depending on the whether or not the discovered factory for this
   * class is scoped (={@link javax.inject.Scope} annotated), the provider will be scoped or not. If
   * it is scoped, it will be scoped in the appropriate scope, if not it will be added to the pool
   * of un-scoped providers. Note that
   *
   * @param clazz the {@link Class} of {@code T} for which we lookup an {@link
   *     InternalProviderImpl}.
   * @param bindingName the potential name of the provider when it was bound (which means we always
   *     returned a scoped provider if name is not null).
   * @param <T> the type for which we lookup an {@link InternalProviderImpl}.
   * @return a provider associated to the {@code T}. The returned provider is un-scoped (remember
   *     that {@link ScopedProviderImpl} is a subclass of {@link InternalProviderImpl}). The
   *     returned provider will be scoped by the public methods to use the current scope.
   */
  /* @VisibleForTesting */ <T> InternalProviderImpl<? extends T> lookupProvider(
      Class<T> clazz, String bindingName) {
    if (clazz == null) {
      throw new IllegalArgumentException("TP can't get an instance of a null class.");
    }
    InternalProviderImpl<? extends T> scopedProvider = getScopedProvider(clazz, bindingName);
    if (scopedProvider != null) {
      return scopedProvider;
    }
    Iterator<ScopeNode> iterator = parentScopes.iterator();
    while (iterator.hasNext()) {
      Scope parentScope = iterator.next();
      ScopeImpl parentScopeImpl = (ScopeImpl) parentScope;
      InternalProviderImpl<? extends T> parentScopedProvider =
          parentScopeImpl.getScopedProvider(clazz, bindingName);
      if (parentScopedProvider != null) {
        return parentScopedProvider;
      }
    }

    // if the binding is named
    // we couldn't find it in any scope, we must fail
    // as only unnamed bindings can be created dynamically
    if (bindingName != null) {
      throw new RuntimeException(
          format(
              "No binding was defined for class %s and name %s " //
                  + "in scope %s and its parents %s",
              clazz.getName(), bindingName, getName(), getParentScopesNames()));
    }

    // we now look for an unnamed binding
    // bindingName = null;  <-- valid but fails checkstyle as we use null directly

    // check if we have a cached un-scoped provider
    InternalProviderImpl unScopedProviderInPool = getUnUnScopedProvider(clazz, null);
    if (unScopedProviderInPool != null) {
      return unScopedProviderInPool;
    }

    // classes discovered at runtime, not bound by any module
    // they will be a bit slower as we need to get the factory first
    // we need to know whether they are scoped or not, if so we scope them
    // if not, they are place in the pool
    Factory<T> factory = FactoryLocator.getFactory(clazz);

    if (factory.hasScopeAnnotation()) {
      // the new provider will have to work in the current scope
      Scope targetScope = factory.getTargetScope(this);
      ScopedProviderImpl<? extends T> newProvider = new ScopedProviderImpl<>(targetScope, factory);
      // it is bound to its target scope only if it has a scope annotation.
      // lock free installing a provider means there could have been one set concurrently since last
      // testing
      // its value. We allow to return it here
      ScopeImpl targetScopeImpl = (ScopeImpl) targetScope;
      return targetScopeImpl.installScopedProvider(clazz, null, newProvider, false);
    } else {
      // the provider is but in a pool of unscoped providers for later reuse
      final InternalProviderImpl<T> newProvider = new InternalProviderImpl<>(factory);
      // the pool is static as it is accessible from all scopes
      // lock free installing a provider means there could have been one set concurrently since last
      // testing
      // its value. We allow to return it here
      return installUnScopedProvider(clazz, null, newProvider);
    }
  }

  /**
   * Obtains the provider of the class {@code clazz} and name {@code bindingName}, if any. The
   * returned provider will be scoped. It can be {@code null} if there is no such provider.
   * Ancestors are not taken into account.
   *
   * @param clazz the class for which to obtain the scoped provider.
   * @param bindingName the name, possibly {@code null}, for which to obtain the scoped provider.
   * @param <T> the type of {@code clazz}.
   * @return the scoped provider for class {@code clazz} and {@code bindingName}. Returns {@code
   *     null} is there is no such scoped provider.
   */
  private <T> InternalProviderImpl<? extends T> getScopedProvider(
      Class<T> clazz, String bindingName) {
    return getInternalProvider(clazz, bindingName, true);
  }

  /**
   * Obtains the provider of the class {@code clazz} and name {@code bindingName}, if any. The
   * returned provider will belong to the pool of unscoped providers. It can be {@code null} if
   * there is no such provider.
   *
   * @param clazz the class for which to obtain the unscoped provider.
   * @param bindingName the name, possibly {@code null}, for which to obtain the unscoped provider.
   * @param <T> the type of {@code clazz}.
   * @return the unscoped provider for class {@code clazz} and {@code bindingName}. Returns {@code
   *     null} is there is no such unscoped provider.
   */
  private <T> InternalProviderImpl<? extends T> getUnUnScopedProvider(
      Class<T> clazz, String bindingName) {
    return getInternalProvider(clazz, bindingName, false);
  }

  /**
   * Obtains the provider of the class {@code clazz} and name {@code bindingName}. The returned
   * provider can either be scoped to the scope or not depending on {@code isScoped}. Ancestors are
   * not taken into account.
   *
   * @param clazz the class for which to obtain the provider.
   * @param bindingName the name, possibly {@code null}, for which to obtain the provider.
   * @param <T> the type of {@code clazz}.
   * @return the provider for class {@code clazz} and {@code bindingName}, either from the set of
   *     providers scoped to the scope or from the pool of unscoped providers. If there is no such
   *     provider, returns {@code null}.
   *     <p>Note to maintainers : we don't use this method directly, both {@link #getScopedProvider}
   *     and {@link #getUnUnScopedProvider} are a facade of this method and make the calls more
   *     clear.
   */
  private <T> InternalProviderImpl<? extends T> getInternalProvider(
      Class<T> clazz, String bindingName, boolean isScoped) {
    if (bindingName == null) {
      if (isScoped) {
        synchronized (mapClassesToUnNamedScopedProviders) {
          return mapClassesToUnNamedScopedProviders.get(clazz);
        }
      } else {
        synchronized (mapClassesToUnNamedUnScopedProviders) {
          return mapClassesToUnNamedUnScopedProviders.get(clazz);
        }
      }
    } else {
      synchronized (mapClassesToNamedScopedProviders) {
        Map<String, InternalProviderImpl> mapNameToProvider =
            mapClassesToNamedScopedProviders.get(clazz);
        if (mapNameToProvider == null) {
          return null;
        }
        return mapNameToProvider.get(bindingName);
      }
    }
  }

  /**
   * Install the provider of the class {@code clazz} and name {@code bindingName} in the current
   * scope.
   *
   * @param clazz the class for which to install the scoped provider.
   * @param bindingName the name, possibly {@code null}, for which to install the scoped provider.
   * @param internalProvider the internal provider to install.
   * @param isTestProvider whether or not is a test provider, installed through a Test Module that
   *     should override existing providers for the same class-bindingname.
   * @param <T> the type of {@code clazz}.
   */
  private <T> InternalProviderImpl<? extends T> installScopedProvider(
      Class<T> clazz,
      String bindingName,
      InternalProviderImpl<? extends T> internalProvider,
      boolean isTestProvider) {
    return installInternalProvider(clazz, bindingName, internalProvider, true, isTestProvider);
  }

  /**
   * Install the provider of the class {@code clazz} and name {@code bindingName} in the pool of
   * unscoped providers.
   *
   * @param clazz the class for which to install the provider.
   * @param bindingName the name, possibly {@code null}, for which to install the scoped provider.
   * @param internalProvider the internal provider to install.
   * @param <T> the type of {@code clazz}.
   */
  private <T> InternalProviderImpl<? extends T> installUnScopedProvider(
      Class<T> clazz, String bindingName, InternalProviderImpl<? extends T> internalProvider) {
    return installInternalProvider(clazz, bindingName, internalProvider, false, false);
  }

  /**
   * Installs a provider either in the scope or the pool of unscoped providers.
   *
   * @param clazz the class for which to install the provider.
   * @param bindingName the name, possibly {@code null}, for which to install the scoped provider.
   * @param internalProvider the internal provider to install.
   * @param isScoped whether or not the provider belongs to a specific scope or belongs to the pool
   *     of unscoped providers.
   * @param isTestProvider whether or not is a test provider, installed through a Test Module that
   *     should override existing providers for the same class-bindingname.
   * @param <T> the type of {@code clazz}.
   *     <p>Note to maintainers : we don't use this method directly, both {@link
   *     #installScopedProvider(Class, String, InternalProviderImpl, boolean)} and {@link
   *     #installUnScopedProvider(Class, String, InternalProviderImpl)} are a facade of this method
   *     and make the calls more clear.
   */
  private <T> InternalProviderImpl<? extends T> installInternalProvider(
      Class<T> clazz,
      String bindingName,
      InternalProviderImpl<? extends T> internalProvider,
      boolean isScoped,
      boolean isTestProvider) {
    if (bindingName == null) {
      if (isScoped) {
        return installUnNamedScopedProvider(
            mapClassesToUnNamedScopedProviders, clazz, internalProvider, isTestProvider);
      } else {
        return installUnNamedScopedProvider(
            mapClassesToUnNamedUnScopedProviders, clazz, internalProvider, isTestProvider);
      }
    } else {
      return installNamedScopedProvider(
          mapClassesToNamedScopedProviders, clazz, bindingName, internalProvider, isTestProvider);
    }
  }

  private <T> InternalProviderImpl<? extends T> installNamedScopedProvider(
      IdentityHashMap<Class, Map<String, InternalProviderImpl>> mapClassesToNamedScopedProviders,
      Class<T> clazz,
      String bindingName,
      InternalProviderImpl<? extends T> internalProvider,
      boolean isTestProvider) {
    synchronized (mapClassesToNamedScopedProviders) {
      Map<String, InternalProviderImpl> mapNameToProvider =
          mapClassesToNamedScopedProviders.get(clazz);
      if (mapNameToProvider == null) {
        mapNameToProvider = new HashMap<>(1);
        mapClassesToNamedScopedProviders.put(clazz, mapNameToProvider);
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

  private <T> InternalProviderImpl<? extends T> installUnNamedScopedProvider(
      IdentityHashMap<Class, InternalProviderImpl> mapClassesToUnNamedProviders,
      Class<T> clazz,
      InternalProviderImpl<? extends T> internalProvider,
      boolean isTestProvider) {
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
      throw new IllegalStateException(
          String.format(
              "The scope with name %s has been already closed."
                  + " It can't be used to create new instances.",
              name));
    }
  }

  static void resetUnScopedProviders() {
    mapClassesToUnNamedUnScopedProviders.clear();
  }

  /**
   * Resets the state of the scope. Useful for automation testing when we want to reset the scope
   * used to install test modules.
   */
  @Override
  protected void reset() {
    super.reset();
    mapClassesToNamedScopedProviders.clear();
    mapClassesToUnNamedScopedProviders.clear();
    hasTestModules = false;
    installBindingForScopeClass();
  }

  @Override
  public void release() {
    for (ScopeNode childScope : childrenScopes.values()) {
      childScope.release();
    }

    synchronized (mapClassesToUnNamedScopedProviders) {
      for (InternalProviderImpl internalProvider : mapClassesToUnNamedScopedProviders.values()) {
        if (internalProvider.isReleasable()) {
          internalProvider.release();
        }
      }
    }
    synchronized (mapClassesToNamedScopedProviders) {
      for (Map<String, InternalProviderImpl> mapNameToInternalProvider :
          mapClassesToNamedScopedProviders.values()) {
        for (InternalProviderImpl internalProvider : mapNameToInternalProvider.values()) {
          if (internalProvider.isReleasable()) {
            internalProvider.release();
          }
        }
      }
    }
  }

  /** Install bindings for scope. */
  private void installBindingForScopeClass() {
    // it's always possible to get access to the scope that contains an injected object.
    installScopedProvider(Scope.class, null, new InternalProviderImpl<>(this), false);
  }

  private static class ClassNameComparator implements Comparator<Class> {
    @Override
    public int compare(Class o1, Class o2) {
      return o1.getName().compareTo(o2.getName());
    }
  }
}
