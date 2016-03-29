package toothpick.integration.data;

import toothpick.Injector;
import toothpick.MemberInjector;

@SuppressWarnings("unused") public class IFooProviderAnnotatedSingleton$$MemberInjector implements MemberInjector<IFooProviderAnnotatedSingleton> {
  @Override public void inject(IFooProviderAnnotatedSingleton foo, Injector injector) {
    foo.bar = injector.createInstance(Bar.class);
  }
}
