package toothpick.integration.data;

import toothpick.Factory;
import toothpick.Injector;

@SuppressWarnings("unused") public class IFooProviderAnnotatedProvidesSingleton$$Factory implements Factory<IFooProviderAnnotatedProvidesSingleton> {
  @Override public IFooProviderAnnotatedProvidesSingleton createInstance(Injector injector) {
    IFooProviderAnnotatedProvidesSingleton iFooProviderAnnotatedSingleton = new IFooProviderAnnotatedProvidesSingleton();
    injector.inject(iFooProviderAnnotatedSingleton);
    return iFooProviderAnnotatedSingleton;
  }

  @Override public boolean hasSingletonAnnotation() {
    return false;
  }

  @Override public boolean hasProducesSingletonAnnotation() {
    return true;
  }
}
