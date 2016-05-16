package toothpick;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import toothpick.config.Binding;
import toothpick.registries.FactoryRegistryLocator;
import toothpick.registries.MemberInjectorRegistryLocator;

import static java.lang.String.format;

/**
 * Strategy pattern that allows to change various behaviors of Toothpick.
 * The default configuration is {@link #production()}.
 * A custom configuration can be created and used by toothpick,
 * it is even possible to use a composition of the built-in configurations.
 */
public abstract class Configuration {

  public static Configuration instance = production();

  public abstract void checkIllegalBinding(Binding binding);

  public abstract void checkCyclesStart(Class clazz);

  public abstract void checkCyclesEnd(Class clazz);

  public abstract <T> Factory<T> getFactory(Class<T> clazz);

  public abstract <T> MemberInjector<T> getMemberInjector(Class<T> clazz);

  /**
   * Allows to pass custom configurations.
   *
   * @param configuration the configuration to use
   */
  static void setConfiguration(Configuration configuration) {
    instance = configuration;
  }

  /**
   * Performs many runtime checks. This configuration
   * reduces performances. It should be used only during development.
   * The checks performed are:
   * <ul>
   * <li>cycle detection: check that not 2 classes depend on each other. Note that if of them uses a Lazy instance
   * of the other or a Producer, then there is no such cycle.</li>
   * <li>illegal binding detection: check no scope annotated class is used as the target of a binding.</li>
   * </ul>
   * This configuration uses reflection.
   *
   * @return a development configuration.
   */
  public static Configuration development() {
    return new DevelopmentConfiguration();
  }

  /**
   * Performs no runtime checks. This configuration
   * is fast but less than {@link #reflectionFree()}.
   * It can be used in production.
   * This configuration uses reflection.
   *
   * @return a production configuration.
   */
  public static Configuration production() {
    return new ProductionConfiguration();
  }

  /**
   * Performs no runtime checks. This configuration
   * is the fastest but will need additional setup
   * of the annotation processors.
   * It can be used in production.
   * This configuration doesn't use reflection.
   *
   * @return an optimized reflection free configuration.
   */
  public static Configuration reflectionFree() {
    return new ReflectionFreeConfiguration();
  }

  private static class BaseConfiguration extends Configuration {

    @Override
    public void checkIllegalBinding(Binding binding) {
    }

    @Override
    public void checkCyclesStart(Class clazz) {
    }

    @Override
    public void checkCyclesEnd(Class clazz) {
    }

    @Override
    public <T> Factory<T> getFactory(Class<T> clazz) {
      return FactoryRegistryLocator.getFactoryUsingReflection(clazz);
    }

    @Override
    public <T> MemberInjector<T> getMemberInjector(Class<T> clazz) {
      return MemberInjectorRegistryLocator.getMemberInjectorUsingReflection(clazz);
    }
  }

  private static class DevelopmentConfiguration extends Configuration {
    // We need a LIFO structure here, but stack is thread safe and we use thread local,
    // so this property is overkill and LinkedHashSet is faster on retrieval.
    private ThreadLocal<LinkedHashSet<Class>> cycleDetectionStack = new ThreadLocal<LinkedHashSet<Class>>() {
      @Override
      protected LinkedHashSet<Class> initialValue() {
        return new LinkedHashSet<>();
      }
    };

    @Override
    public void checkIllegalBinding(Binding binding) {
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
    public void checkCyclesStart(Class clazz) {
      if (cycleDetectionStack.get().contains(clazz)) {
        throw new CyclicDependencyException(new ArrayList<>(cycleDetectionStack.get()), clazz);
      }

      cycleDetectionStack.get().add(clazz);
    }

    @Override
    public void checkCyclesEnd(Class clazz) {
      cycleDetectionStack.get().remove(clazz);
    }

    @Override
    public <T> Factory<T> getFactory(Class<T> clazz) {
      return FactoryRegistryLocator.getFactoryUsingReflection(clazz);
    }

    @Override
    public <T> MemberInjector<T> getMemberInjector(Class<T> clazz) {
      return MemberInjectorRegistryLocator.getMemberInjectorUsingReflection(clazz);
    }
  }

  private static class ProductionConfiguration extends BaseConfiguration {
    @Override
    public <T> Factory<T> getFactory(Class<T> clazz) {
      return FactoryRegistryLocator.getFactoryUsingReflection(clazz);
    }
  }

  private static class ReflectionFreeConfiguration extends BaseConfiguration {
    @Override
    public <T> Factory<T> getFactory(Class<T> clazz) {
      return FactoryRegistryLocator.getFactoryUsingRegistries(clazz);
    }

    @Override
    public <T> MemberInjector<T> getMemberInjector(Class<T> clazz) {
      return MemberInjectorRegistryLocator.getMemberInjectorUsingRegistries(clazz);
    }
  }
}
