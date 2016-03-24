package toothpick.config.bindings;

import toothpick.config.Binding;

/**
 * Created by snicolas on 3/24/16.
 */
public class SingletonBinding extends Binding {
  public <T> SingletonBinding(Class<T> key) {
    super(key);
  }
}
