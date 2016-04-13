package toothpick.registries.memberscope;

import java.util.ArrayList;
import java.util.List;
import toothpick.MemberInjector;
import toothpick.registries.MemberInjectorRegistry;

/**
 * The base class of the {@link MemberInjectorRegistry} classes generated by the toothpick annotation processor.
 * Those {@link MemberInjectorRegistry} can form a tree of {@link MemberInjectorRegistry} and have the ability to run through the tree
 * of their children {@link MemberInjectorRegistry} to find a {@link MemberInjector}.
 *
 * This class is meant to be used internally by toothpick.
 */
public abstract class AbstractMemberInjectorRegistry implements MemberInjectorRegistry {

  private List<MemberInjectorRegistry> childrenRegistries = new ArrayList<>();

  public void addChildRegistry(MemberInjectorRegistry childRegistry) {
    this.childrenRegistries.add(childRegistry);
  }

  /**
   * Explore the subtree of children registries via DFS to retrieve a {@link MemberInjector} for a given class.
   * This method will be called automatically by all member scopes generated by toothpick, if it is provided
   * appropriate parameters when compiling each library.
   *
   * @param clazz the class to look the {@link MemberInjector} for.
   * @param <T> the type of {@code clazz}.
   * @return an instance of the {@link MemberInjector} class asssociated with {@code clazz} or null if it can't find one.
   */
  protected <T> MemberInjector<T> getMemberInjectorInChildrenRegistries(Class<T> clazz) {
    MemberInjector<T> memberInjector;
    for (MemberInjectorRegistry registry : childrenRegistries) {
      memberInjector = registry.getMemberInjector(clazz);
      if (memberInjector != null) {
        return memberInjector;
      }
    }
    return null;
  }
}
