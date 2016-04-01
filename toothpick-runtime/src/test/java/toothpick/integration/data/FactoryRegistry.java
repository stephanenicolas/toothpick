package toothpick.integration.data;

import toothpick.Factory;
import toothpick.registries.factory.AbstractFactoryRegistry;

public class FactoryRegistry extends AbstractFactoryRegistry {

  @Override public <T> Factory<T> getFactory(Class<T> clazz) {
    switch (clazz.getName()) {
      case "toothpick.integration.data.Bar":
        return (Factory<T>) new Bar$$Factory();
      case "toothpick.integration.data.Foo":
        return (Factory<T>) new Foo$$Factory();
      case "toothpick.integration.data.IFooProvider":
        return (Factory<T>) new IFooProvider$$Factory();
      case "toothpick.integration.data.IFooWithBarProvider":
        return (Factory<T>) new IFooWithBarProvider$$Factory();
      case "toothpick.integration.data.IFooProviderAnnotatedProvidesSingleton":
        return (Factory<T>) new IFooProviderAnnotatedProvidesSingleton$$Factory();
      case "toothpick.integration.data.IFooProviderAnnotatedSingleton":
        return (Factory<T>) new IFooProviderAnnotatedSingleton$$Factory();
      default:
        return getFactoryInChildrenRegistries(clazz);
    }
  }
}
