package toothpick;

import java.util.Stack;
import toothpick.config.Binding;

import static java.lang.String.format;

public abstract class Configuration {

  public static volatile Configuration INSTANCE;

  abstract void checkIllegalBinding(Binding binding);

  abstract void checkCyclesStart(Class clazz);

  abstract void checkCyclesEnd();

  static void development() {
    INSTANCE = new Configuration() {
      private Stack<Class> cycleDetectionStack = new Stack<>();

      @Override
      void checkIllegalBinding(Binding binding) {

      }

      @Override
      void checkCyclesStart(Class clazz) {
        if (cycleDetectionStack.contains(clazz)) {
          //TODO make the message better
          throw new CyclicDependencyException(format("Class % creates a cycle", clazz.getName()));
        }

        cycleDetectionStack.push(clazz);
      }

      @Override
      void checkCyclesEnd() {
        cycleDetectionStack.pop();
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
      void checkCyclesStart(Class clazz) {
        //do nothing
      }

      @Override
      void checkCyclesEnd() {
        //do nothing
      }
    };
  }

  static void setConfiguration(Configuration configuration) {
    INSTANCE = configuration;
  }
}
