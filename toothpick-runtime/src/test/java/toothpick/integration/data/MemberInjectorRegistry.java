package toothpick.integration.data;

import toothpick.MemberInjector;
import toothpick.registries.memberinjector.AbstractMemberInjectorRegistry;

public class MemberInjectorRegistry extends AbstractMemberInjectorRegistry {

  @Override public <T> MemberInjector<T> getMemberInjector(Class<T> clazz) {
    switch (clazz.getName()) {
      case "toothpick.integration.data.Foo":
        return (MemberInjector<T>) new Foo$$MemberInjector();
      case "toothpick.integration.data.FooChildWithInjectedFields":
        return (MemberInjector<T>) new FooChildWithInjectedFields$$MemberInjector();
      case "toothpick.integration.data.FooGrandChildWithInjectedFields":
        return (MemberInjector<T>) new FooGrandChildWithInjectedFields$$MemberInjector();
      case "toothpick.integration.data.FooProvider":
        return (MemberInjector<T>) new FooProvider$$MemberInjector();
      case "toothpick.integration.data.FooSingleton":
        return (MemberInjector<T>) new FooSingleton$$MemberInjector();
      case "toothpick.integration.data.FooWithFuture":
        return (MemberInjector<T>) new FooWithFuture$$MemberInjector();
      case "toothpick.integration.data.FooWithLazy":
        return (MemberInjector<T>) new FooWithLazy$$MemberInjector();
      case "toothpick.integration.data.FooWithProvider":
        return (MemberInjector<T>) new FooWithProvider$$MemberInjector();
      case "toothpick.integration.data.FooWithProviderOfSingleton":
        return (MemberInjector<T>) new FooWithProviderOfSingleton$$MemberInjector();
      case "toothpick.integration.data.IFooProvider":
        return (MemberInjector<T>) new IFooProvider$$MemberInjector();
      case "toothpick.integration.data.IFooProviderAnnotatedProvidesSingleton":
        return (MemberInjector<T>) new IFooProviderAnnotatedProvidesSingleton$$MemberInjector();
      case "toothpick.integration.data.IFooProviderAnnotatedSingleton":
        return (MemberInjector<T>) new IFooProviderAnnotatedSingleton$$MemberInjector();
      default:
        return super.getMemberInjector(clazz);
    }
  }
}
