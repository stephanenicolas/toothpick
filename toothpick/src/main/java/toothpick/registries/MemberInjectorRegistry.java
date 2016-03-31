package toothpick.registries;

import toothpick.MemberInjector;

/**
 * A component that can retrieve a {@link MemberInjector} for a given class.
 * The annotation processor can generate classes that implement this interface
 * if it passed some arguments. This interface will not really be used by developers,
 * they will use once each of the generated subclasses.
 *
 * See the MemberInjectorLocator java docs in toothpick-runtime package.
 */
public interface MemberInjectorRegistry {
  <T> MemberInjector<T> getMemberInjector(Class<T> clazz);
}
