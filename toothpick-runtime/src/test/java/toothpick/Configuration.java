package toothpick;

import toothpick.config.Binding;

public abstract class Configuration {

  public static volatile Configuration INSTANCE;

  abstract void checkIllegalBinding(Binding binding);

  abstract void checkCycles();

  static void development() {
    INSTANCE = new Configuration() {
      @Override
      void checkIllegalBinding(Binding binding) {

      }

      @Override
      void checkCycles() {

      }
    };
  }

  static void production() {
    INSTANCE = new Configuration() {
      @Override
      void checkIllegalBinding(Binding binding) {
        //do nothing
      }

      @Override
      void checkCycles() {
        //do nothing
      }
    };
  }

  static void setConfiguration(Configuration configuration) {
    INSTANCE = configuration;
  }
}
