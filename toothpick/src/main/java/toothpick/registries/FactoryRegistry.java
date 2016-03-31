package toothpick.registries;

import toothpick.Factory;

/**
 * A component that can retrieve a {@link Factory} for a given class.
 * The annotation processor can generate classes that implement this interface
 * if it passed some arguments. This interface will not really be used by developers,
 * they will use once each of the generated subclasses.
 *
 * See the FactoryRegistryLocator java docs in toothpick-runtime package.
 */
public interface FactoryRegistry {
  <T> Factory<T> getFactory(Class<T> clazz);
}
