/**
 * Retrieve instance of factories.
 */
public interface FactoryRegistry {
  <T> Factory<T> getFactory(Class<T> clazz);
}
