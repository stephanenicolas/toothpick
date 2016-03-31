package toothpick.registries.factory;

import java.util.ArrayList;
import java.util.List;
import toothpick.Factory;
import toothpick.MemberInjector;

/**
 * Retrieve instance of factories.
 * TODO get rid of reflection.
 * The plan is to use a tree of AbstractMemberInjectorRegistry :
 * when a lib is compiled, we pass an argument to the processor
 * that creates the AbstractMemberInjectorRegistry in a given package. It can have dependencies :
 * other member injector registries.
 * A member injector registry will care about the class it knows the member injector of, and can
 * delegate to its dependencies when it doesn't know the member injector.
 *
 * @see MemberInjector
 */
public abstract class AbstractFactoryRegistry {

  private List<AbstractFactoryRegistry> childrenRegistries = new ArrayList<>();

  public void addChildRegistry(AbstractFactoryRegistry childRegistry) {
    this.childrenRegistries.add(childRegistry);
  }

  public <T> Factory<T> getFactory(Class<T> clazz) {
    Factory<T> factory;
    for (AbstractFactoryRegistry registry : childrenRegistries) {
      factory = registry.getFactory(clazz);
      if (factory != null) {
        return factory;
      }
    }
    return null;
  }
}
