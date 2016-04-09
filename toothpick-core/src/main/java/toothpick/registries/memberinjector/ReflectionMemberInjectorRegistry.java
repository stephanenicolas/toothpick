package toothpick.registries.memberinjector;

import toothpick.Factory;
import toothpick.MemberInjector;

/**
 * Finds instances of {@link Factory} via reflection.
 */
public class ReflectionMemberInjectorRegistry extends AbstractMemberInjectorRegistry {

  public <T> MemberInjector<T> getMemberInjector(Class<T> clazz) {
    if (clazz == null) {
      throw new IllegalArgumentException("Class can't be null");
    }

    System.out.printf("Warning class %s has no registered memberInjector, falling back on reflection. This slows down your app.\n", clazz);
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
