package toothpick;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import toothpick.config.Binding;
import toothpick.config.Module;
import toothpick.providers.FactoryPoweredProvider;
import toothpick.providers.SingletonPoweredProvider;

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
      Provider provider = binding.toProvider();
      provider.setInjector(this);
    }
  }

  @Override
  public Injector getParent() {
    return parent;
  }

  @Override
  public Object getKey() {
    return key;
  }

  @Override
  public <T> T getScopedInstance(Class<T> clazz) {
    Provider<T> provider = scope.get(clazz);
    if (provider == null) {
      return null;
    }
    return provider.get();
  }

  @Override
  public void inject(Object obj) {

  }

  public IdentityHashMap<Class, Provider> getScope() {
    return scope;
  }

  @Override
  public <T> T createInstance(Class<T> clazz) {
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
    while( currentInjector != null ) {
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
}
