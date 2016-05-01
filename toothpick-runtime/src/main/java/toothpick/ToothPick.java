package toothpick;

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Main class to access toothpick features.
 * It allows to create / retrieve scopes and perform injections.
 *
 * The main rule about using TP is : <b>TP will honor all injections in the instances it creates by itself.</b><br/>
 * <em/>A soon as you use {@code new Foo}, in a provider or a binding for instance, TP is not responsible for injecting Foo;
 * developers have to manually inject the instances they create.</em> <br/
 */
public final class ToothPick {

  private static final ConcurrentHashMap<Object, Scope> MAP_KEY_TO_SCOPE = new ConcurrentHashMap<>();
  private static Injector injector = new InjectorImpl();

  private ToothPick() {
    throw new RuntimeException("Constructor can't be invoked even via reflection.");
  }

  /**
   * Opens multiple scopes in a row.
   * Opened scopes will be children of each other in left to right order (e.g. {@code openScopes(a,b)} opens scopes {@code a} and {@code b}
   * and {@code b} is a child of {@code a}.
   *
   * @param names of the scopes to open hierarchically.
   * @return the last opened scope, leaf node of the created subtree of scopes.
   */
  public static Scope openScopes(Object... names) {

    if (names == null) {
      throw new IllegalArgumentException("null scopes can't be open.");
    }

    Scope previousScope;
    Scope lastScope = null;
    for (Object name : names) {
      previousScope = lastScope;
      lastScope = openScope(name);
      if (previousScope != null) {
        previousScope.addChild(lastScope);
      }
    }

    return lastScope;
  }

  /**
   * Opens a scope without any parent.
   * If a scope by this {@code name} already exists, it is returned.
   * Otherwise a new scope is created.
   */
  public static Scope openScope(Object name) {
    Scope scope = MAP_KEY_TO_SCOPE.get(name);
    if (scope != null) {
      return scope;
    }
    scope = new ScopeImpl(name);
    Scope previous = MAP_KEY_TO_SCOPE.putIfAbsent(name, scope);
    if (previous != null) {
      scope = previous;
    }
    return scope;
  }

  /**
   * Detach a scope from its parent, this will trigger the garbage collection of this scope and it's sub-scopes
   * if they are not referenced outside of ToothPick.
   *
   * @param name the name of the scope to close.
   */

  public static synchronized void closeScope(Object name) {
    Scope scope = MAP_KEY_TO_SCOPE.remove(name);
    if (scope != null) {
      Scope parentScope = scope.getParentScope();
      if (parentScope != null) {
        parentScope.removeChild(scope);
      }
      removeScopeAndChildrenFromMap(scope);
    }
  }

  /**
   * Clears all scopes. Useful for testing and not getting any leak...
   */

  public static void reset() {
    MAP_KEY_TO_SCOPE.clear();
  }

  /**
   * Injects all dependencies (transitively) in {@code obj}, dependencies will be obtained in the scope {@code scope}.
   *
   * @param obj the object to be injected.
   * @param scope the scope in which  all dependencies are obtained.
   */
  public static void inject(Object obj, Scope scope) {
    injector.inject(obj, scope);
  }

  // Not synchronized, called by closeScope that is synchronized
  private static void removeScopeAndChildrenFromMap(Scope scope) {
    MAP_KEY_TO_SCOPE.remove(scope.getName());
    for (Scope childScope : scope.childrenScopes) {
      removeScopeAndChildrenFromMap(childScope);
    }
  }

  public static Collection<Object> getScopeNames() {
    return MAP_KEY_TO_SCOPE.keySet();
  }
}
