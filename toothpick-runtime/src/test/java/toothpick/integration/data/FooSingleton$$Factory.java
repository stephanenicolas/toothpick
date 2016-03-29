package toothpick.integration.data;

import toothpick.Factory;
import toothpick.Injector;

@SuppressWarnings("unused") public class FooSingleton$$Factory implements Factory<FooSingleton> {
  @Override public FooSingleton createInstance(Injector injector) {
    FooSingleton foo = new FooSingleton();
    injector.inject(foo);
    return foo;
  }

  @Override public boolean hasSingletonAnnotation() {
    return true;
  }

  @Override public boolean hasProducesSingletonAnnotation() {
    return false;
  }
}
