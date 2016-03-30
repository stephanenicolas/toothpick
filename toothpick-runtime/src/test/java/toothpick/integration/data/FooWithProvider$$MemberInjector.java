package toothpick.integration.data;

import javax.inject.Provider;
import toothpick.Injector;
import toothpick.MemberInjector;

@SuppressWarnings("unused") public class FooWithProvider$$MemberInjector implements MemberInjector<FooWithProvider> {
  @Override public void inject(FooWithProvider foo, Injector injector) {
    foo.bar = injector.getProvider(Bar.class);
  }
}
