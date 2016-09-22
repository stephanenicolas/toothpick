package toothpick.configuration;

import toothpick.Scope;
import toothpick.config.Binding;

class RuntimeCheckOffConfiguration implements RuntimeCheckConfiguration {
  @Override
  public void checkIllegalBinding(Binding binding, Scope scope) {
  }

  @Override
  public void checkCyclesStart(Class clazz, String name) {
  }

  @Override
  public void checkCyclesEnd(Class clazz, String name) {
  }
}
