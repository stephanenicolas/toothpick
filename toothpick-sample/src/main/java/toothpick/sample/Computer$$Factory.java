package toothpick.sample;

import toothpick.Factory;
import toothpick.Injector;

public class Computer$$Factory implements Factory<Computer> {
  @Override public Computer createInstance(Injector injector) {
    return new Computer();
  }

  @Override public boolean hasSingletonAnnotation() {
    return false;
  }

  @Override public boolean hasProducesSingletonAnnotation() {
    return false;
  }
}
