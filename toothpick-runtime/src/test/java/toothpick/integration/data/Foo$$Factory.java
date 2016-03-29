package toothpick.integration.data;

import toothpick.Factory;
import toothpick.Injector;

@SuppressWarnings("unused") public class Foo$$Factory implements Factory<Foo> {
  @Override public Foo createInstance(Injector injector) {
    Foo foo = new Foo();
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
