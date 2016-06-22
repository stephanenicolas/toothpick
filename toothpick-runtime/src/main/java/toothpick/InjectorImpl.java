package toothpick;

import toothpick.registries.MemberInjectorRegistryLocator;

/**
 * Default implementation of an injector.
 */
public final class InjectorImpl implements Injector {
  /**
   * {@inheritDoc}
   *
   * <p>
   * <em><b>&#9888; Warning</b> &#9888; : This implementation needs a proper setup of {@link toothpick.registries.MemberInjectorRegistry} instances
   * at the annotation processor level. We will bubble up the hierarchy, starting at class {@code T}. As soon
   * as a {@link MemberInjector} is found, we use it to inject the members of {@code obj}. If registries are not find then
   * you will observe strange behaviors at runtime : only members defined in super class will be injected.</em>
   * </p>
   */
  @Override
  public <T> void inject(T obj, Scope scope) {
    Class<? super T> currentClass = (Class<T>) obj.getClass();
    do {
      MemberInjector<? super T> memberInjector = MemberInjectorRegistryLocator.getMemberInjector(currentClass);
      if (memberInjector != null) {
        memberInjector.inject(obj, scope);
        return;
      } else {
        currentClass = currentClass.getSuperclass();
      }
    } while (currentClass != null);
  }
}
