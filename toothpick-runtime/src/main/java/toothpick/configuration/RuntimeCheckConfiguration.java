package toothpick.configuration;

import toothpick.config.Binding;

interface RuntimeCheckConfiguration {
  void checkIllegalBinding(Binding binding);
  void checkCyclesStart(Class clazz, String name);
  void checkCyclesEnd(Class clazz, String name);
}
