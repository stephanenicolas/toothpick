package toothpick;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import javax.inject.Provider;
import toothpick.config.Binding;
import toothpick.config.Module;
import toothpick.providers.FactoryPoweredProvider;
import toothpick.providers.NonAnnotatedProviderClassPoweredProvider;
import toothpick.providers.NonSingletonAnnotatedClassPoweredProvider;
import toothpick.providers.ProducesSingletonAnnotatedProviderClassPoweredProvider;
import toothpick.providers.SingletonAnnotatedClassPoweredProvider;
import toothpick.providers.SingletonAnnotatedProviderClassPoweredProvider;
import toothpick.providers.SingletonPoweredProvider;
import toothpick.registries.factory.FactoryRegistryLocator;
import toothpick.registries.memberinjector.MemberInjectorRegistryLocator;

import static java.lang.String.format;

/**
 * This class should never be used outside of the toothpick library.
 */
public class InjectorImpl implements Injector {
  public static final ExecutorService EXECUTOR_SERVICE = Executors.newFixedThreadPool(4);
  private IdentityHashMap<Class, Provider> scope = new IdentityHashMap<>();
  private Injector parent;
  //parentInjectors, sorted from this the root injector
  private final List<InjectorImpl> parentInjectors;
  private boolean hasOverrides;

  public InjectorImpl(Injector parent, Module... modules) {
    this.parent = parent;
    if (parent == null) {
      parentInjectors = new ArrayList<>();
    } else {
      parentInjectors = new ArrayList<>(((InjectorImpl) parent).parentInjectors);
    }
    parentInjectors.add(0, this);
    installModules(modules);
  }

  public InjectorImpl(Module... modules) {
    this(null, modules);
  }

  @Override public Injector getParent() {
    return parent;
  }

  @Override public <T> void inject(T obj) {
    MemberInjector<T> memberInjector = MemberInjectorRegistryLocator.getMemberInjector((Class<T>) obj.getClass());
    memberInjector.inject(obj, this);
  }

  public IdentityHashMap<Class, Provider> getScope() {
    return scope;
  }

  @Override public <T> T getInstance(Class<T> clazz) {
    synchronized (clazz) {
      Iterator<InjectorImpl> iterator = parentInjectors.iterator();
      while (iterator.hasNext()) {
        InjectorImpl parentInjector = iterator.next();
        T scopedInstance = parentInjector.getScopedInstance(clazz);
        if (scopedInstance != null) {
          return scopedInstance;
        }
      }
    }
    Factory<T> factory = FactoryRegistryLocator.getFactory(clazz);
    T instance = factory.createInstance(this);
    if (factory.hasSingletonAnnotation()) {
      //singleton classes discovered dynamically go to root scope.
      getRootInjector().getScope().put(clazz, new SingletonPoweredProvider(instance));
    } else {
      scope.put(clazz, new FactoryPoweredProvider(this, factory));
    }
    return instance;
  }

  private InjectorImpl getRootInjector() {
    return parentInjectors.get(parentInjectors.size() - 1);
  }

  @Override public <T> Provider<T> getProvider(Class<T> clazz) {
    synchronized (clazz) {
      Iterator<InjectorImpl> iterator = parentInjectors.iterator();
      while (iterator.hasNext()) {
        InjectorImpl parentInjector = iterator.next();
        Provider<T> scopedProvider = parentInjector.getScopedProvider(clazz);
        if (scopedProvider != null) {
          return scopedProvider;
        }
      }
    }
    Factory<T> factory = FactoryRegistryLocator.getFactory(clazz);
    T instance = factory.createInstance(this);
    final Provider<T> newProvider;
    if (factory.hasSingletonAnnotation()) {
      //singleton classes discovered dynamically go to root scope.
      newProvider = new SingletonPoweredProvider(instance);
      getRootInjector().getScope().put(clazz, newProvider);
    } else {
      newProvider = new FactoryPoweredProvider(this, factory);
      scope.put(clazz, newProvider);
    }
    return newProvider;
  }

  @Override public <T> Lazy<T> getLazy(Class<T> clazz) {
    Provider<T> provider = getProvider(clazz);
    return new LazyImpl<>(provider);
  }

  @Override public <T> Future<T> getFuture(Class<T> clazz) {
    final Provider<T> provider = getProvider(clazz);
    return EXECUTOR_SERVICE.submit(new ProviderCallable<>(provider));
  }

  @Override public void installOverrideModules(Module... modules) {
    //we allow multiple calls to this method
    boolean oldHasOverrides = hasOverrides;
    hasOverrides = false;
    installModules(modules);
    boolean doOverrideModulesExist = modules != null;
    hasOverrides = oldHasOverrides || doOverrideModulesExist;
  }

  private void installModule(Module module) {
    for (Binding binding : module.getBindingSet()) {
      if (!hasOverrides || !scope.containsKey(binding.getKey())) {
        scope.put(binding.getKey(), toProvider(binding));
      }
    }
  }

  /**
   * Obtains the instance of the class {@code clazz} that is scoped in the current scope, if any.
   * Ancestors are not taken into account.
   *
   * @param clazz the class for which to obtain the scoped instance of this injector, if one is scoped.
   * @param <T> the type of {@code clazz}.
   * @return the scoped instance of this injector, if one is scoped, {@code Null} otherwise.
   */
  private <T> T getScopedInstance(Class<T> clazz) {
    Provider<T> provider = scope.get(clazz);
    if (provider == null) {
      return null;
    }
    return provider.get();
  }

  /**
   * Obtains the provider of the class {@code clazz} that is scoped in the current scope, if any.
   * Ancestors are not taken into account.
   *
   * @param clazz the class for which to obtain the scoped provider of this injector, if one is scoped.
   * @param <T> the type of {@code clazz}.
   * @return the scoped provider of this injector, if one is scoped, {@code Null} otherwise.
   */
  private <T> Provider<T> getScopedProvider(Class<T> clazz) {
    return scope.get(clazz);
  }

  private void installModules(Module[] modules) {
    for (Module module : modules) {
      installModule(module);
    }
  }

  //do not change the return type to Provider<? extends T>.
  //it would be cool and more convenient for bindings, but it would
  //make the APIs very unstable as you could not get any instance of the
  //implementation class via an injector, it would fail but be syntactically valid.
  //only creating an instance of the interface is valid with this syntax.
  /*VisibleForTesting*/ <T> Provider<T> toProvider(Binding<T> binding) {
    if (binding == null) {
      throw new IllegalStateException("null binding are not allowed. Should not happen unless getBindingSet is overridden.");
    }
    switch (binding.getMode()) {
      case SIMPLE:
        Factory<? extends T> factory = FactoryRegistryLocator.getFactory(binding.getKey());
        if (factory.hasSingletonAnnotation()) {
          return new SingletonAnnotatedClassPoweredProvider<>(this, binding.getKey(), binding.getKey());
        } else {
          return new NonSingletonAnnotatedClassPoweredProvider<>(this, binding.getKey(), binding.getKey());
        }
      case CLASS:
        Factory<? extends T> factory2 = FactoryRegistryLocator.getFactory(binding.getImplementationClass());
        if (factory2.hasSingletonAnnotation()) {
          return new SingletonAnnotatedClassPoweredProvider(this, binding.getKey(), binding.getImplementationClass());
        } else {
          return new NonSingletonAnnotatedClassPoweredProvider<>(this, binding.getKey(), binding.getImplementationClass());
        }
      case INSTANCE:
        return new SingletonPoweredProvider<>(binding.getInstance());
      case PROVIDER_INSTANCE:
        return binding.getProviderInstance();
      case PROVIDER_CLASS:
        Factory<? extends Provider<T>> providerFactory = FactoryRegistryLocator.getFactory(binding.getProviderClass());
        //TODO pass them the factory !
        if (providerFactory.hasSingletonAnnotation()) {
          return new SingletonAnnotatedProviderClassPoweredProvider(this, binding.getKey(), binding.getProviderClass());
        } else if (providerFactory.hasProducesSingletonAnnotation()) {
          return new ProducesSingletonAnnotatedProviderClassPoweredProvider<>(this, binding.getKey(), binding.getProviderClass());
        } else {
          return new NonAnnotatedProviderClassPoweredProvider<>(this, binding.getKey(), binding.getProviderClass());
        }

        //JACOCO:OFF
      default:
        throw new IllegalStateException(format("mode is not handled: %s. This should not happen.", binding.getMode()));
        //JACOCO:ON
    }
  }
}
