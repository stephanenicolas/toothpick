package toothpick;

import java.util.Iterator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.inject.Provider;
import toothpick.config.Binding;
import toothpick.config.Module;
import toothpick.registries.factory.FactoryRegistryLocator;

import static java.lang.String.format;

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
    return getProvider(clazz).get();
  }

  @Override
  public <T> Provider<T> getProvider(Class<T> clazz) {
    return getProvider(clazz, null);
  }

  @Override
  public <T> Lazy<T> getLazy(Class<T> clazz) {
    return getLazy(clazz, null);
  }

  @Override
  public <T> T getInstance(Class<T> clazz, String name) {
    return getProvider(clazz, name).get();
  }

  @Override
  public <T> Provider<T> getProvider(Class<T> clazz, String name) {
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
      Factory<T> factory = FactoryRegistryLocator.getFactory(clazz);
      final Provider<T> newProvider;
      if (factory.hasSingletonAnnotation()) {
        //singleton classes discovered dynamically go to root scope.
        newProvider = new ProviderImpl<>(factory.createInstance(this));
        getRootScope().installProvider(clazz, name, newProvider);
      } else {
        newProvider = new ProviderImpl(this, factory, false);
        installProvider(clazz, name, newProvider);
      }
      return newProvider;
    }
  }

  @Override
  public <T> Lazy<T> getLazy(Class<T> clazz, String name) {
    Provider<T> provider = getProvider(clazz, name);
    return new ProviderImpl<>(provider, true);
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

      Class key = binding.getKey();
      synchronized (key) {
        String bindingName = binding.getName();
        if (!hasTestModules || getScopedProvider(key, bindingName) == null) {
          Provider provider = toProvider(binding);
          installProvider(key, bindingName, provider);
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
        Factory<? extends T> factory = FactoryRegistryLocator.getFactory(binding.getKey());
        return new ProviderImpl<>(this, factory, false);
      case CLASS:
        Factory<? extends T> factory2 = FactoryRegistryLocator.getFactory(binding.getImplementationClass());
        return new ProviderImpl<>(this, factory2, false);
      case INSTANCE:
        return new ProviderImpl<>(binding.getInstance());
      case PROVIDER_INSTANCE:
        //to ensure providers do not have to deal with concurrency, we wrap them in a thread safe provider
        return new ProviderImpl<>(binding.getProviderInstance(), false);
      case PROVIDER_CLASS:
        Factory<? extends Provider<T>> providerFactory = FactoryRegistryLocator.getFactory(binding.getProviderClass());
        return new ProviderImpl<>(this, providerFactory, true);

      //JACOCO:OFF
      default:
        throw new IllegalStateException(format("mode is not handled: %s. This should not happen.", binding.getMode()));
        //JACOCO:ON
    }
  }
}
