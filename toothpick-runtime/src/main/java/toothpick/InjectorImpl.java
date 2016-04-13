package toothpick;

import toothpick.registries.memberinjector.MemberInjectorRegistryLocator;

/**
 * Default implementation of an scope.
 */
public final class InjectorImpl implements Injector {
  @Override
  public <T> void inject(T obj, Scope scope) {
    MemberInjector<T> memberInjector = MemberInjectorRegistryLocator.getMemberInjector((Class<T>) obj.getClass());
    memberInjector.inject(obj, scope);
  }
}
