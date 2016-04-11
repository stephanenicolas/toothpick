package toothpick.registries.memberinjector;

import java.util.ArrayList;
import java.util.List;
import toothpick.MemberInjector;
import toothpick.registries.FactoryRegistry;
import toothpick.registries.MemberInjectorRegistry;

/**
 * Locates the {@link MemberInjectorRegistry} instances.
 * Works in the same way as {@link FactoryRegistry}.
 *
 * @see FactoryRegistry
 * @see MemberInjectorRegistry
 * @see MemberInjector
 */
public class MemberInjectorRegistryLocator {
  private MemberInjectorRegistryLocator() {
  }

  private static ReflectionMemberInjectorRegistry fallbackMemberInjectorRegistry = new ReflectionMemberInjectorRegistry();
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
    return fallbackMemberInjectorRegistry.getMemberInjector(clazz);
  }
}
