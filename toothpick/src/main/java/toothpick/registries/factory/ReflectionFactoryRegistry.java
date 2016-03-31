package toothpick.registries.factory;

import toothpick.Factory;

/**
 * Retrieve instance of factories.
 * TODO get rid of reflection.
 * The plan is to use a tree of FactoryRegistry :
 * when a lib is compiled, we pass an argument to the processor
 * that creates the FactoryRegistry in a given package. It can have dependencies :
 * other factory registries.
 * A factory registry will care about the class it knows the factory of, and can
 * delegate to its dependencies when it doesn't know the factory.
 *
 * @see Factory
 */
public class ReflectionFactoryRegistry extends AbstractFactoryRegistry {
  public <T> Factory<T> getFactory(Class<T> clazz) {
    if (clazz == null) {
      throw new IllegalArgumentException("Class can't be null");
    }

    System.out.printf("Warning class %s has no generated factory, falling back on reflection. This slows down your app.\n", clazz);
    try {
      Class<Factory<T>> factoryClass = (Class<Factory<T>>) Class.forName(clazz.getName() + "$$Factory");
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
