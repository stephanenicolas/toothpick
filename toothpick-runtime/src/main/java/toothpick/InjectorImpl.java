package toothpick;

import toothpick.registries.memberinjector.MemberInjectorRegistryLocator;

/**
 * Default implementation of an injector.
 */
public final class InjectorImpl implements Injector {
  @Override
  public <T> void inject(T obj, Scope scope) {
    inject((Class<T>) obj.getClass(), obj, scope);
  }

  @Override
  public <T> void inject(Class<T> clazz, T obj, Scope scope) {
    MemberInjector<T> memberInjector = MemberInjectorRegistryLocator.getMemberInjector(clazz);
    memberInjector.inject(obj, scope);
  }
}
