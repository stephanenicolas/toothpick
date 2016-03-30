package toothpick.integration.data;

import toothpick.Injector;
import toothpick.MemberInjector;

@SuppressWarnings("unused") public class FooProvider$$MemberInjector implements MemberInjector<FooProvider> {
  @Override public void inject(FooProvider foo, Injector injector) {
    foo.bar = injector.getInstance(Bar.class);
  }
}
