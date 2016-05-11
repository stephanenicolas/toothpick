package toothpick.concurrency.utils;

import toothpick.Factory;
import toothpick.concurrency.BindingsMultiThreadTest;
import toothpick.registries.FactoryRegistry;

public class DynamicTestClassesFactoryRegistry implements FactoryRegistry {
  private boolean scoped;

  public DynamicTestClassesFactoryRegistry(boolean scoped) {
    this.scoped = scoped;
  }

  @Override
  public <T> Factory<T> getFactory(final Class<T> clazz) {
    if (clazz.getName().startsWith("Class_")) {
      return new DynamicTestClassesFactory<>(clazz, scoped);
    }
    return null;
  }
}
