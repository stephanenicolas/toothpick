package toothpick.concurrency.utils;

import toothpick.Factory;
import toothpick.Scope;

public class DynamicTestClassesFactory<T> implements Factory<T> {
  private final Class<T> clazz;
  private boolean scoped;

  public DynamicTestClassesFactory(Class<T> clazz, boolean scoped) {
    this.clazz = clazz;
    this.scoped = scoped;
  }

  @Override
  public T createInstance(Scope scope) {
    try {
      return clazz.newInstance();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public Scope getTargetScope(Scope currentScope) {
    return currentScope;
  }

  @Override
  public boolean hasScopeAnnotation() {
    return scoped;
  }

  @Override
  public boolean hasScopeInstancesAnnotation() {
    return false;
  }
}
