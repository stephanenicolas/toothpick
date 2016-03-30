package toothpick.integration.data;

import toothpick.Injector;
import toothpick.MemberInjector;

@SuppressWarnings("unused") public class FooWithFuture$$MemberInjector implements MemberInjector<FooWithFuture> {
  @Override public void inject(FooWithFuture foo, Injector injector) {
    foo.bar = injector.getFuture(Bar.class);
  }
}
