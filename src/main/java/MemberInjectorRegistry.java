/**
 * Retrieve instance of factories.
 */
public interface MemberInjectorRegistry {
  <T> MemberInjector<T> getMemberInjector(Class<T> clazz);
}
