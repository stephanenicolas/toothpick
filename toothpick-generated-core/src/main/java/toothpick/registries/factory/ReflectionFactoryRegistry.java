package toothpick.registries.factory;

import toothpick.Factory;
import toothpick.registries.FactoryRegistry;

/**
 * Finds instances of {@link Factory} via reflection.
 */
public class ReflectionFactoryRegistry implements FactoryRegistry {

  public static final String FACTORY_SUFFIX = "$$Factory";

  public <T> Factory<T> getFactory(Class<T> clazz) {
    if (clazz == null) {
      throw new IllegalArgumentException("Class can't be null");
    }

    System.out.printf("Warning class %s has no registered factory, falling back on reflection. This slows down your app.\n", clazz);
    try {
      Class<Factory<T>> factoryClass = (Class<Factory<T>>) Class.forName(clazz.getName() + FACTORY_SUFFIX);
      return factoryClass.newInstance();
    } catch (ClassNotFoundException e) {
      throw new RuntimeException("Impossible to get the factory class for class " + clazz.getName() + ". Add an inject annotated constructor.");
    } catch (InstantiationException e) {
      throw new RuntimeException("This should not happen. Impossible to create factory for class " + clazz.getName());
    } catch (IllegalAccessException e) {
      throw new RuntimeException("This should not happen. Impossible to access factory constructor for class " + clazz.getName());
    }
  }
}
