package toothpick.configuration;

import toothpick.Scope;
import toothpick.config.Binding;

/**
 * Defines a check strategy.
 */
interface RuntimeCheckConfiguration {
  /**
   * Checks that a binding is legal in the current scope.
   *
   * @param binding the binding being installed.
   * @param scope the scope where the binding is installed.
   */
  void checkIllegalBinding(Binding binding, Scope scope);

  /**
   * Called when the class {@code class} starts being injected
   * using the qualifier {@code name}. Will check whether or not
   * there is a cycle in the dependencies of this injection
   * (i.e. a dependency transitively needs itself).
   * @param clazz the class to be injected.
   * @param name the name of the required injection.
   */
  void checkCyclesStart(Class clazz, String name);

  /**
   * Called when the class {@code class} ends being injected
   * using the qualifier {@code name}. Will check whether or not
   * there is a cycle in the dependencies of this injection
   * (i.e. a dependency transitively needs itself).
   * @param clazz the class to be injected.
   * @param name the name of the required injection.
   */
  void checkCyclesEnd(Class clazz, String name);
}
