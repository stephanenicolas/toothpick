package toothpick.registries.memberinjector;

import java.util.ArrayList;
import java.util.List;
import toothpick.MemberInjector;

/**
 * Retrieve instance of factories.
 * TODO get rid of reflection.
 * The plan is to use a tree of AbstractMemberInjectorRegistry :
 * when a lib is compiled, we pass an argument to the processor
 * that creates the AbstractMemberInjectorRegistry in a given package. It can have dependencies :
 * other member injector registries.
 * A member injector registry will care about the class it knows the member injector of, and can
 * delegate to its dependencies when it doesn't know the member injector.
 *
 * @see MemberInjector
 */
public class MemberInjectorRegistryLocator {
  private MemberInjectorRegistryLocator() {
  }

  private static ReflectionMemberInjectorRegistry fallbackMemberInjectorRegistry = new ReflectionMemberInjectorRegistry();
  private static List<AbstractMemberInjectorRegistry> registries = new ArrayList<>();

  public static void addRegistry(AbstractMemberInjectorRegistry childRegistry) {
    registries.add(childRegistry);
  }

  public static <T> MemberInjector<T> getMemberInjector(Class<T> clazz) {
    MemberInjector<T> memberInjector;
    for (AbstractMemberInjectorRegistry registry : registries) {
      memberInjector = registry.getMemberInjector(clazz);
      if (memberInjector != null) {
        return memberInjector;
      }
    }
    return fallbackMemberInjectorRegistry.getMemberInjector(clazz);
  }
}
