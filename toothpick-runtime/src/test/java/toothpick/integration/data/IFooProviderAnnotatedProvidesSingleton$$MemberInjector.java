package toothpick.integration.data;

import toothpick.Injector;
import toothpick.MemberInjector;

@SuppressWarnings("unused") public class IFooProviderAnnotatedProvidesSingleton$$MemberInjector
    implements MemberInjector<IFooProviderAnnotatedProvidesSingleton> {
  @Override public void inject(IFooProviderAnnotatedProvidesSingleton foo, Injector injector) {
    foo.bar = injector.getInstance(Bar.class);
  }
}
