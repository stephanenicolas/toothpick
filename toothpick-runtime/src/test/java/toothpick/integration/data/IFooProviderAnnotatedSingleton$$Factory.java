package toothpick.integration.data;

import toothpick.Factory;
import toothpick.Injector;

@SuppressWarnings("unused") public class IFooProviderAnnotatedSingleton$$Factory implements Factory<IFooProviderAnnotatedSingleton> {
  @Override public IFooProviderAnnotatedSingleton createInstance(Injector injector) {
    IFooProviderAnnotatedSingleton iFooProviderAnnotatedSingleton = new IFooProviderAnnotatedSingleton();
    injector.inject(iFooProviderAnnotatedSingleton);
    return iFooProviderAnnotatedSingleton;
  }

  @Override public boolean hasSingletonAnnotation() {
    return true;
  }

  @Override public boolean hasProducesSingletonAnnotation() {
    return false;
  }
}
