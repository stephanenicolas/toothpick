package toothpick.integration.data;

import toothpick.Injector;
import toothpick.MemberInjector;

@SuppressWarnings("unused") public class IFooWithBarProvider$$MemberInjector implements MemberInjector<IFooWithBarProvider> {
  @Override public void inject(IFooWithBarProvider foo, Injector injector) {
    foo.bar = injector.getInstance(Bar.class);
  }
}
