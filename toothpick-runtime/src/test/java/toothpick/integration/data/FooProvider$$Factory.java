package toothpick.integration.data;

import toothpick.Factory;
import toothpick.Injector;

@SuppressWarnings("unused") public class FooProvider$$Factory implements Factory<FooProvider> {
  @Override public FooProvider createInstance(Injector injector) {
    FooProvider foo = new FooProvider();
    injector.inject(foo);
    return foo;
  }

  @Override public boolean hasSingletonAnnotation() {
    return false;
  }

  @Override public boolean hasProducesSingletonAnnotation() {
    return false;
  }
}
