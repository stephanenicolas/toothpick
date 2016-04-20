package toothpick;

import java.util.Iterator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
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
  public static final ExecutorService EXECUTOR_SERVICE = Executors.newFixedThreadPool(4);
  private boolean hasTestModules;

  public ScopeImpl(Object name) {
    super(name);
    //it's always possible to get access to the scope that contains an injected object.
    installProvider(Scope.class, null, new ProviderImpl<>(this));
  }

  @Override
  public <T> T getInstance(Class<T> clazz) {
    return getProviderInternal(clazz, null).get();
  }

  @Override
  public <T> T getInstance(Class<T> clazz, String name) {
    return getProviderInternal(clazz, name).get();
  }

  @Override
  public <T> Provider<T> getProvider(Class<T> clazz) {
    Provider<T> provider = getProviderInternal(clazz, null);
    return new ThreadSafeProviderImpl<>(provider, false);
  }

  @Override
  public <T> Provider<T> getProvider(Class<T> clazz, String name) {
    Provider<T> provider = getProviderInternal(clazz, name);
    return new ThreadSafeProviderImpl<>(provider, false);
  }

  @Override
  public <T> Lazy<T> getLazy(Class<T> clazz) {
    Provider<T> provider = getProviderInternal(clazz, null);
    return new ThreadSafeProviderImpl<>(provider, true);
  }

  @Override
  public <T> Lazy<T> getLazy(Class<T> clazz, String name) {
    Provider<T> provider = getProviderInternal(clazz, name);
    return new ThreadSafeProviderImpl<>(provider, true);
  }

  public <T> Provider<T> getProviderInternal(Class<T> clazz, String name) {
    if (clazz == null) {
      throw new IllegalArgumentException("TP can't get an instance of a null class.");
    }
    synchronized (clazz) {
      Provider<T> scopedProvider = getScopedProvider(clazz, name);
      if (scopedProvider != null) {
        return scopedProvider;
      }
      Iterator<Scope> iterator = parentScopes.iterator();
      while (iterator.hasNext()) {
        Scope parentScope = iterator.next();
        Provider<T> parentScopedProvider = parentScope.getScopedProvider(clazz, name);
        if (parentScopedProvider != null) {
          return parentScopedProvider;
        }
      }

      //classes discovered at runtime, not bound by any module
      //they will be a bit slower as we need to get the factory first
      Factory<T> factory = FactoryRegistryLocator.getFactory(clazz);
      final Provider<T> newProvider = new ProviderImpl<>(this, factory, false);
      if (factory.hasSingletonAnnotation()) {
        //singleton classes discovered dynamically go to root scope.
        getRootScope().installProvider(clazz, name, newProvider);
      } else {
        installProvider(clazz, name, newProvider);
      }
      return newProvider;
    }
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

  private void installModule(Module module) {
    for (Binding binding : module.getBindingSet()) {
      if (binding == null) {
        throw new IllegalStateException("A module can't have a null binding.");
      }

      Class clazz = binding.getKey();
      synchronized (clazz) {
        String bindingName = binding.getName();
        if (!hasTestModules || getScopedProvider(clazz, bindingName) == null) {
          Provider provider = toProvider(binding);
          installProvider(clazz, bindingName, provider);
        }
      }
    }
  }

  //do not change the return type to Provider<? extends T>.
  //it would be cool and more convenient for bindings, but it would
  //make the APIs very unstable as you could not get any instance of the
  //implementation class via an scope, it would fail but be syntactically valid.
  //only creating an instance of the interface is valid with this syntax.
  /*VisibleForTesting*/ <T> Provider<T> toProvider(Binding<T> binding) {
    if (binding == null) {
      throw new IllegalStateException("null binding are not allowed. Should not happen unless getBindingSet is overridden.");
    }
    switch (binding.getMode()) {
      case SIMPLE:
        return new ProviderImpl<>(this, binding.getKey(), false);
      case CLASS:
        return new ProviderImpl<>(this, binding.getImplementationClass(), false);
      case INSTANCE:
        return new ProviderImpl<>(binding.getInstance());
      case PROVIDER_INSTANCE:
        //to ensure providers do not have to deal with concurrency, we wrap them in a thread safe provider
        return new ProviderImpl<>(binding.getProviderInstance(), false);
      case PROVIDER_CLASS:
        return new ProviderImpl<>(this, binding.getProviderClass(), true);

      //JACOCO:OFF
      default:
        throw new IllegalStateException(format("mode is not handled: %s. This should not happen.", binding.getMode()));
        //JACOCO:ON
    }
  }
}
