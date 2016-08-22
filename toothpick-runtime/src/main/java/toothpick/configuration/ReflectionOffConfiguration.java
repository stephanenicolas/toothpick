package toothpick.configuration;

import toothpick.Factory;
import toothpick.MemberInjector;
import toothpick.registries.FactoryRegistryLocator;
import toothpick.registries.MemberInjectorRegistryLocator;

class ReflectionOffConfiguration implements ReflectionConfiguration {
  @Override
  public <T> Factory<T> getFactory(Class<T> clazz) {
    return FactoryRegistryLocator.getFactoryUsingRegistries(clazz);
  }

  @Override
  public <T> MemberInjector<T> getMemberInjector(Class<T> clazz) {
    return MemberInjectorRegistryLocator.getMemberInjectorUsingRegistries(clazz);
  }
}
