package toothpick.integration.data;

import toothpick.Injector;
import toothpick.MemberInjector;
import toothpick.registries.memberinjector.MemberInjectorRegistryLocator;

//as parent doesn't define inject annotated fields, we jump to grand parent member injector.
@SuppressWarnings("unused") public class FooGrandChildWithInjectedFields$$MemberInjector implements MemberInjector<FooGrandChildWithInjectedFields> {
  private MemberInjector<Foo> fooMemberInjector = MemberInjectorRegistryLocator.getMemberInjector(Foo.class);

  @Override public void inject(FooGrandChildWithInjectedFields foo, Injector injector) {
    foo.bar2 = injector.getInstance(Bar.class);
    fooMemberInjector.inject(foo, injector);
  }
}

