package toothpick.integration.data;

import toothpick.Injector;
import toothpick.MemberInjector;

@SuppressWarnings("unused") public class FooWithProviderOfSingleton$$MemberInjector implements MemberInjector<FooWithProviderOfSingleton> {
  @Override public void inject(FooWithProviderOfSingleton foo, Injector injector) {
    foo.fooSingletonProvider = injector.getProvider(FooSingleton.class);
  }
}
