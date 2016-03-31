package toothpick.integration.data;

import toothpick.Factory;
import toothpick.Injector;

@SuppressWarnings("unused") public class IFooWithBarProvider$$Factory implements Factory<IFooWithBarProvider> {
  @Override public IFooWithBarProvider createInstance(Injector injector) {
    IFooWithBarProvider iFooProvider = new IFooWithBarProvider();
    injector.inject(iFooProvider);
    return iFooProvider;
  }

  @Override public boolean hasSingletonAnnotation() {
    return false;
  }

  @Override public boolean hasProducesSingletonAnnotation() {
    return false;
  }
}
