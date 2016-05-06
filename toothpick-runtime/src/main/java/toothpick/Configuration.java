package toothpick;

import java.util.Stack;
import java.util.concurrent.locks.ReentrantLock;
import toothpick.config.Binding;

import static java.lang.String.format;

public abstract class Configuration {

  /*package-private.*/ static Configuration instance;

  abstract void checkIllegalBinding(Binding binding);

  abstract void checkCyclesStart(Class clazz);

  abstract void checkCyclesEnd();

  static {
    //default mode is production
    instance = production();
  }

  public static Configuration development() {
    return new Configuration() {
      private Stack<Class> cycleDetectionStack = new Stack<>();
      //used to make the cycle check atomic.
      private ReentrantLock reentrantLock = new ReentrantLock();

      @Override
      void checkIllegalBinding(Binding binding) {

      }

      @Override
      void checkCyclesStart(Class clazz) {
        if (cycleDetectionStack.contains(clazz)) {
          //TODO make the message better
          throw new CyclicDependencyException(format("Class %s creates a cycle", clazz.getName()));
        }

        cycleDetectionStack.push(clazz);
        reentrantLock.lock();
      }

      @Override
      void checkCyclesEnd() {
        cycleDetectionStack.pop();
        reentrantLock.unlock();
      }
    };
  }

  public static Configuration production() {
    return new Configuration() {
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

  /**
   * Allows to pass custom configurations.
   *
   * @param configuration the configuration to use
   */
  static void setConfiguration(Configuration configuration) {
    instance = configuration;
  }
}
