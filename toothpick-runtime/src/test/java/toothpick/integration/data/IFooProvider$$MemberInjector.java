package toothpick.integration.data;

import toothpick.Injector;
import toothpick.MemberInjector;

@SuppressWarnings("unused") public class IFooProvider$$MemberInjector implements MemberInjector<IFooProvider> {
  @Override public void inject(IFooProvider foo, Injector injector) {
    foo.bar = injector.getInstance(Bar.class);
  }
}
