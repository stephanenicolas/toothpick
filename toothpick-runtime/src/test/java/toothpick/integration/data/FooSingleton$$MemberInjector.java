package toothpick.integration.data;

import toothpick.Injector;
import toothpick.MemberInjector;

@SuppressWarnings("unused") public class FooSingleton$$MemberInjector implements MemberInjector<FooSingleton> {
  @Override public void inject(FooSingleton foo, Injector injector) {
    foo.bar = injector.createInstance(Bar.class);
  }
}
