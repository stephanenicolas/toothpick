package toothpick;

/**
 * Retrieve instance of factories.
 * @see FactoryRegistry
 * TODO document properly
 */
public class MemberInjectorRegistry {
  private MemberInjectorRegistry() {
  }

  public static <T> MemberInjector<T> getMemberInjector(Class<T> clazz) {
    if (clazz == null) {
      throw new IllegalArgumentException("Class can't be null");
    }

    try {
      Class<MemberInjector<T>> memberInjectorClass =
          (Class<MemberInjector<T>>) Class.forName(clazz.getName() + "$$MemberInjector");
      return memberInjectorClass.newInstance();
    } catch (ClassNotFoundException e) {
      throw new RuntimeException("Impossible to get the member injector class for class "
          + clazz.getName()
          + ". Add an inject annotated field.");
    } catch (InstantiationException e) {
      throw new RuntimeException(
          "This should not happen. Impossible to create member injector for class "
              + clazz.getName());
    } catch (IllegalAccessException e) {
      throw new RuntimeException(
          "This should not happen. Impossible to access member injector constructor for class "
              + clazz.getName());
    }
  }
}
