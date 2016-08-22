package toothpick.configuration;

import toothpick.Factory;
import toothpick.MemberInjector;
import toothpick.registries.FactoryRegistryLocator;
import toothpick.registries.MemberInjectorRegistryLocator;

class ReflectionOnConfiguration implements ReflectionConfiguration {
  @Override
  public <T> Factory<T> getFactory(Class<T> clazz) {
    return FactoryRegistryLocator.getFactoryUsingReflection(clazz);
  }

  @Override
  public <T> MemberInjector<T> getMemberInjector(Class<T> clazz) {
    return MemberInjectorRegistryLocator.getMemberInjectorUsingReflection(clazz);
  }
}
