package toothpick.config.bindings;

import toothpick.config.Binding;

public class SingletonBinding extends Binding {
  public <T> SingletonBinding(Class<T> key) {
    super(key);
  }
}
