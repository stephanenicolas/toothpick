package toothpick;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import toothpick.config.Binding;
import toothpick.config.Module;
import toothpick.providers.FactoryPoweredProvider;
import toothpick.providers.NonAnnotatedProviderClassPoweredProvider;
import toothpick.providers.NonSingletonAnnotatedClassPoweredProvider;
import toothpick.providers.ProducesSingletonAnnotatedProviderClassPoweredProvider;
import toothpick.providers.SingletonAnnotatedClassPoweredProvider;
import toothpick.providers.SingletonAnnotatedProviderClassPoweredProvider;
import toothpick.providers.SingletonPoweredProvider;

import static java.lang.String.format;

/**
 * This class should never be used outside of the toothpick library.
 */
public class InjectorImpl implements Injector {
  private IdentityHashMap<Class, Provider> scope = new IdentityHashMap<>();
  private Injector parent;
  private Object key;
  private final List<Injector> parentInjectors;

  public InjectorImpl(Injector parent, Object key, Module... modules) {
    this.parent = parent;
    this.key = key;
    parentInjectors = getParentInjectors();
    installModules(modules);
  }

  public void installModule(Module module) {
    for (Binding binding : module.getBindingSet()) {
      Provider provider = toProvider(binding);
      provider.setInjector(this);
    }
  }

  @Override public Injector getParent() {
    return parent;
  }

  @Override public Object getKey() {
    return key;
  }

  @Override public <T> T getScopedInstance(Class<T> clazz) {
    Provider<T> provider = scope.get(clazz);
    if (provider == null) {
      return null;
    }
    return provider.get();
  }

  @Override public <T> void inject(T obj) {
    MemberInjector<T> memberInjector =
        MemberInjectorRegistry.getMemberInjector((Class<T>) obj.getClass());
    memberInjector.inject(obj, this);
  }

  public IdentityHashMap<Class, Provider> getScope() {
    return scope;
  }

  @Override public <T> T createInstance(Class<T> clazz) {
    synchronized (clazz) {
      for (Injector parentInjector : parentInjectors) {
        T scopedInstance = parentInjector.getScopedInstance(clazz);
        if (scopedInstance != null) {
          return scopedInstance;
        }
      }
    }
    Factory<T> factory = FactoryRegistry.getFactory(clazz);
    T instance = factory.createInstance(this);
    if (factory.hasSingletonAnnotation()) {
      scope.put(clazz, new SingletonPoweredProvider(instance));
    } else {
      scope.put(clazz, new FactoryPoweredProvider(factory, this));
    }
    return instance;
  }

  private List<Injector> getParentInjectors() {
    List<Injector> parentInjectors = new ArrayList<>();
    Injector currentInjector = this;
    while (currentInjector != null) {
      parentInjectors.add(0, currentInjector);
      currentInjector = currentInjector.getParent();
    }
    return parentInjectors;
  }

  private void installModules(Module[] modules) {
    for (Module module : modules) {
      installModule(module);
    }
  }

  private <T> Provider<T> toProvider(Binding<T> binding) {
    switch (binding.getMode()) {
      case SIMPLE:
        return new SingletonAnnotatedClassPoweredProvider<>(binding.getKey(), binding.getKey());
      case CLASS:
        Factory<? extends T> factory = FactoryRegistry.getFactory(binding.getImplementationClass());
        if (factory.hasSingletonAnnotation()) {
          return new SingletonAnnotatedClassPoweredProvider(binding.getKey(), binding.getImplementationClass());
        } else {
          return new NonSingletonAnnotatedClassPoweredProvider<>(binding.getKey(), binding.getImplementationClass());
        }
      case INSTANCE:
        return new SingletonPoweredProvider<>(binding.getInstance());
      case PROVIDER_INSTANCE:
        return binding.getProviderInstance();
      case PROVIDER_CLASS:
        Factory<? extends Provider<T>> providerFactory = FactoryRegistry.getFactory(binding.getProviderClass());
        if (providerFactory.hasSingletonAnnotation()) {
          return new SingletonAnnotatedProviderClassPoweredProvider(binding.getKey(), binding.getProviderClass());
        } else if (providerFactory.hasProducesSingletonAnnotation()) {
          return new ProducesSingletonAnnotatedProviderClassPoweredProvider<>(binding.getKey(), binding.getProviderClass());
        } else {
          return new NonAnnotatedProviderClassPoweredProvider<>(binding.getKey(), binding.getProviderClass());
        }

      default:
        throw new IllegalStateException(format("mode is not handled: %s. This should not happen.", binding.getMode()));
    }
  }

}
