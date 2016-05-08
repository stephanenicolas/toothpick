package toothpick.registries;

import toothpick.MemberInjector;

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
    MemberInjector<T> memberInjector;
    if (registry != null) {
      memberInjector = registry.getMemberInjector(clazz);
      if (memberInjector != null) {
        return memberInjector;
      }
    }
    return null;
  }
}
