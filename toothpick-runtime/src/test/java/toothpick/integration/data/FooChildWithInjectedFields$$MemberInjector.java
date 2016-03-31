package toothpick.integration.data;

import toothpick.Injector;
import toothpick.MemberInjector;
import toothpick.registries.memberinjector.MemberInjectorRegistryLocator;

@SuppressWarnings("unused") public class FooChildWithInjectedFields$$MemberInjector implements MemberInjector<FooChildWithInjectedFields> {
  private MemberInjector<Foo> fooMemberInjector = MemberInjectorRegistryLocator.getMemberInjector(Foo.class);

  @Override public void inject(FooChildWithInjectedFields foo, Injector injector) {
    foo.bar2 = injector.getInstance(Bar.class);
    fooMemberInjector.inject(foo, injector);
  }
}

