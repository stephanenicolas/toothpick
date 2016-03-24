package toothpick.config;

import java.util.HashSet;
import java.util.Set;
import toothpick.config.bindings.SingletonBinding;

/**
 * Created by snicolas on 3/24/16.
 */
public class Module {
  private Set<Binding> bindingSet = new HashSet<>();


  public <T> Binding<T> bind(Class<T> key) {
    Binding<T> binding = new Binding<>(key);
    bindingSet.add(binding);
    return binding;
  }

  public Set<Binding> getBindingSet() {
    return bindingSet;
  }
}
