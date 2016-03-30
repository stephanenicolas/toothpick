package toothpick.integration.data;

import toothpick.Injector;
import toothpick.MemberInjector;
import toothpick.MemberInjectorRegistry;

@SuppressWarnings("unused") public class FooChildWithInjectedFields$$MemberInjector implements MemberInjector<FooChildWithInjectedFields> {
  private MemberInjector<Foo> fooMemberInjector = MemberInjectorRegistry.getMemberInjector(Foo.class);

  @Override public void inject(FooChildWithInjectedFields foo, Injector injector) {
    foo.bar2 = injector.getInstance(Bar.class);
    fooMemberInjector.inject(foo, injector);
  }
}

