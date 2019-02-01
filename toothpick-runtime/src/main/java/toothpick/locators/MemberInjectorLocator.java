package toothpick.locators;

import toothpick.MemberInjector;

/**
 * Locates the {@link MemberInjector} instances.
 * If not {@link MemberInjector} is found, we simply return {@code null}.
 * This is required to fully support polymorphism when injecting dependencies.
 *
 * @see MemberInjector
 */
public class MemberInjectorLocator {
  private MemberInjectorLocator() {
  }

  public static <T> MemberInjector<T> getMemberInjector(Class<T> clazz) {
      try {
          Class<? extends MemberInjector<T>> memberInjectorClass =
                  (Class<? extends MemberInjector<T>>) Class.forName(clazz.getName() + "__MemberInjector");
          return memberInjectorClass.newInstance();
      } catch (Exception e) {
          return null;
      }
  }
}
