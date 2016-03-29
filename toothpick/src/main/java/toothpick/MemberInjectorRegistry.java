package toothpick;

/**
 * Retrieve instance of factories.
 * TODO get rid of reflection.
 * The plan is to use a tree of MemberInjectorRegistry :
 * when a lib is compiled, we pass an argument to the processor
 * that creates the MemberInjectorRegistry in a given package. It can have dependencies :
 * other member injector registries.
 * A member injector registry will care about the class it knows the member injector of, and can
 * delegate to its dependencies when it doesn't know the member injector.
 *
 * @see MemberInjector
 */
public class MemberInjectorRegistry {
  private MemberInjectorRegistry() {
  }

  public static <T> MemberInjector<T> getMemberInjector(Class<T> clazz) {
    if (clazz == null) {
      throw new IllegalArgumentException("Class can't be null");
    }

    try {
      Class<MemberInjector<T>> memberInjectorClass = (Class<MemberInjector<T>>) Class.forName(clazz.getName() + "$$MemberInjector");
      return memberInjectorClass.newInstance();
    } catch (ClassNotFoundException e) {
      throw new RuntimeException("Impossible to get the member injector class for class " + clazz.getName() + ". Add an inject annotated field.");
    } catch (InstantiationException e) {
      throw new RuntimeException("This should not happen. Impossible to create member injector for class " + clazz.getName());
    } catch (IllegalAccessException e) {
      throw new RuntimeException("This should not happen. Impossible to access member injector constructor for class " + clazz.getName());
    }
  }
}
