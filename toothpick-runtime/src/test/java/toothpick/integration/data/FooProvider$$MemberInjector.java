package toothpick.integration.data;

import toothpick.Injector;
import toothpick.MemberInjector;

@SuppressWarnings("unused") public class FooProvider$$MemberInjector implements MemberInjector<FooProvider> {
  @Override public void inject(FooProvider foo, Injector injector) {
    Bar bar = injector.createInstance(Bar.class);
    foo.bar = bar;
  }
}
