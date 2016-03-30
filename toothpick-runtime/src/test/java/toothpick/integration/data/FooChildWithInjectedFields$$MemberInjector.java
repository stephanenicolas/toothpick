package toothpick.integration.data;

import toothpick.Injector;

@SuppressWarnings("unused") public class FooChildWithInjectedFields$$MemberInjector
    extends Foo$$MemberInjector {
  @Override public void inject(Foo foo, Injector injector) {
    ((FooChildWithInjectedFields)foo).bar2 = injector.getInstance(Bar.class);
    super.inject(foo, injector);
  }
}

