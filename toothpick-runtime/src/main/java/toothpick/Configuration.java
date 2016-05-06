package toothpick;

import java.lang.annotation.Annotation;
import java.util.Stack;
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
      private ThreadLocal<Stack<Class>> cycleDetectionStack = new ThreadLocal<Stack<Class>>() {
        @Override
        protected Stack<Class> initialValue() {
          return new Stack<>();
        }
      };

      @Override
      void checkIllegalBinding(Binding binding) {
        Class<?> clazz;
        switch (binding.getMode()) {
          case SIMPLE:
            clazz = binding.getKey();
            break;
          case CLASS:
            clazz = binding.getImplementationClass();
            break;
          case PROVIDER_CLASS:
            clazz = binding.getProviderClass();
            break;
          default:
            return;
        }

        for (Annotation annotation : clazz.getAnnotations()) {
          if (annotation.annotationType().isAnnotationPresent(javax.inject.Scope.class)) {
            throw new IllegalBindingException();
          }
        }
      }

      @Override
      void checkCyclesStart(Class clazz) {
        if (cycleDetectionStack.get().contains(clazz)) {
          //TODO make the message better
          throw new CyclicDependencyException(format("Class %s creates a cycle", clazz.getName()));
        }

        cycleDetectionStack.get().push(clazz);
      }

      @Override
      void checkCyclesEnd() {
        cycleDetectionStack.get().pop();
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
