package toothpick.integration.data;

import toothpick.Injector;
import toothpick.MemberInjector;

@SuppressWarnings("unused") public class Foo$$MemberInjector implements MemberInjector<Foo> {
  @Override public void inject(Foo foo, Injector injector) {
    foo.bar = injector.getInstance(Bar.class);
  }
}
