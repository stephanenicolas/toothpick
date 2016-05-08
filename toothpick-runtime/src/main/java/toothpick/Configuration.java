package toothpick;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import toothpick.config.Binding;

import static java.lang.String.format;

public abstract class Configuration {

  /*package-private.*/ static Configuration instance;

  abstract void checkIllegalBinding(Binding binding);

  abstract void checkCyclesStart(Class clazz);

  abstract void checkCyclesEnd(Class clazz);

  static {
    //default mode is production
    instance = production();
  }

  public static Configuration development() {
    return new Configuration() {
      // We need a LIFO structure here, but stack is thread safe and we use thread local,
      // so this property is overkill and LinkedHashSet is faster on retrieval.
      private ThreadLocal<LinkedHashSet<Class>> cycleDetectionStack = new ThreadLocal<LinkedHashSet<Class>>() {
        @Override
        protected LinkedHashSet<Class> initialValue() {
          return new LinkedHashSet<>();
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
            throw new IllegalBindingException(format("Class %s cannot be bound. It has an scope annotation", clazz.getName()));
          }
        }
      }

      @Override
      void checkCyclesStart(Class clazz) {
        if (cycleDetectionStack.get().contains(clazz)) {
          throw new CyclicDependencyException(new ArrayList<>(cycleDetectionStack.get()), clazz);
        }

        cycleDetectionStack.get().add(clazz);
      }

      @Override
      void checkCyclesEnd(Class clazz) {
        cycleDetectionStack.get().remove(clazz);
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
      void checkCyclesEnd(Class clazz) {
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
