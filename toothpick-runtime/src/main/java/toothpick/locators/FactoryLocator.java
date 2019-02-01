package toothpick.locators;

import toothpick.Factory;

/**
 * The locator retrieves a {@link Factory} for a given class.
 * In case no generated factory for a given class, we throw a {@link NoFactoryFoundException}.
 *
 * @see Factory
 */
public class FactoryLocator {
  private FactoryLocator() {
  }


  public static <T> Factory<T> getFactory(Class<T> clazz) {
    try {
      Class<? extends Factory<T>> factoryClass = (Class<? extends Factory<T>>) Class.forName(clazz.getName() + "__Factory");
      return factoryClass.newInstance();
    } catch (Exception e) {
      throw new NoFactoryFoundException(clazz, e);
    }
  }
}
