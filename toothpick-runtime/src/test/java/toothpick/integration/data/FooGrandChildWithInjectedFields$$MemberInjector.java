package toothpick.integration.data;

import toothpick.Injector;

//as parent doesn't define inject annotated fields, we jump to grand parent member injector.
@SuppressWarnings("unused") public class FooGrandChildWithInjectedFields$$MemberInjector
    extends Foo$$MemberInjector {
  @Override public void inject(Foo foo, Injector injector) {
    ((FooGrandChildWithInjectedFields)foo).bar2 = injector.getInstance(Bar.class);
    super.inject(foo, injector);
  }
}

