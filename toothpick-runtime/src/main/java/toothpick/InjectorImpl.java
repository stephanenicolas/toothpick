package toothpick;

import toothpick.locators.MemberInjectorLocator;

/**
 * Default implementation of an injector.
 */
public final class InjectorImpl implements Injector {
  /**
   * {@inheritDoc}
   *
   * <p>
   * We will bubble up the hierarchy, starting at class {@code T}. As soon as a {@link MemberInjector} is found,
   * we use it to inject the members of {@code obj}.
   * </p>
   */
  @Override
  public <T> void inject(T obj, Scope scope) {
    Class<? super T> currentClass = (Class<T>) obj.getClass();
    do {
      MemberInjector<? super T> memberInjector = MemberInjectorLocator.getMemberInjector(currentClass);
      if (memberInjector != null) {
        memberInjector.inject(obj, scope);
        return;
      } else {
        currentClass = currentClass.getSuperclass();
      }
    } while (currentClass != null);
  }
}
