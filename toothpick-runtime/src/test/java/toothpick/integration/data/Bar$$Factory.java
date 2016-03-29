package toothpick.integration.data;

import toothpick.Factory;
import toothpick.Injector;

@SuppressWarnings("unused") public class Bar$$Factory implements Factory<Bar> {
  @Override public Bar createInstance(Injector injector) {
    return new Bar();
  }

  @Override public boolean hasSingletonAnnotation() {
    return false;
  }

  @Override public boolean hasProducesSingletonAnnotation() {
    return false;
  }
}
