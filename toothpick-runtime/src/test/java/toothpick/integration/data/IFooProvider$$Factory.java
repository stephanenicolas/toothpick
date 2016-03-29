package toothpick.integration.data;

import toothpick.Factory;
import toothpick.Injector;

@SuppressWarnings("unused") public class IFooProvider$$Factory implements Factory<IFooProvider> {
  @Override public IFooProvider createInstance(Injector injector) {
    IFooProvider iFooProvider = new IFooProvider();
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
