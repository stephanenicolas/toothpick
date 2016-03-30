package toothpick.integration.data;

import javax.inject.Provider;
import toothpick.Injector;
import toothpick.Lazy;
import toothpick.MemberInjector;

@SuppressWarnings("unused") public class FooWithLazy$$MemberInjector implements MemberInjector<FooWithLazy> {
  @Override public void inject(FooWithLazy foo, Injector injector) {
    foo.bar = injector.getLazy(Bar.class);
  }
}
