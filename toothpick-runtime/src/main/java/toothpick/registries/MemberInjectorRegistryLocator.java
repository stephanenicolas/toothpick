package toothpick.registries;

import toothpick.MemberInjector;
import toothpick.configuration.ConfigurationHolder;

/**
 * Locates the {@link MemberInjectorRegistry} instances.
 * Works in the same way as {@link FactoryRegistry}, except that is no {@link MemberInjector} is found,
 * we simply return {@code null}. This is required to fully support polymorphism when injecting dependencies.
 *
 * @see FactoryRegistry
 * @see MemberInjectorRegistry
 * @see MemberInjector
 */
public class MemberInjectorRegistryLocator {
  private MemberInjectorRegistryLocator() {
  }

  private static MemberInjectorRegistry registry;

  public static void setRootRegistry(MemberInjectorRegistry registry) {
    MemberInjectorRegistryLocator.registry = registry;
  }

  public static <T> MemberInjector<T> getMemberInjector(Class<T> clazz) {
    return ConfigurationHolder.configuration.getMemberInjector(clazz);
  }

  public static <T> MemberInjector<T> getMemberInjectorUsingRegistries(Class<T> clazz) {
    MemberInjector<T> memberInjector;
    if (registry != null) {
      memberInjector = registry.getMemberInjector(clazz);
      if (memberInjector != null) {
        return memberInjector;
      }
    }
    return null;
  }

  public static <T> MemberInjector<T> getMemberInjectorUsingReflection(Class<T> clazz) {
    try {
      Class<? extends MemberInjector<T>> memberInjectorClass =
          (Class<? extends MemberInjector<T>>) Class.forName(clazz.getName() + "$$MemberInjector");
      return memberInjectorClass.newInstance();
    } catch (Exception e) {
      return null;
    }
  }
}
