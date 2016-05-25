package toothpick;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Main class to access toothpick features.
 * It allows to create / retrieve scopes and perform injections.
 *
 * The main rule about using TP is : <b>TP will honor all injections in the instances it creates by itself.</b><br/>
 * <em/>A soon as you use {@code new Foo}, in a provider or a binding for instance, TP is not responsible for injecting Foo;
 * developers have to manually inject the instances they create.</em> <br/
 */
public final class Toothpick {

  //TP must be lock free, any thread can see the state of tp before or after it is transformed but not during
  //its transformation
  private static final ConcurrentHashMap<Object, Scope> MAP_KEY_TO_SCOPE = new ConcurrentHashMap<>();
  private static Injector injector = new InjectorImpl();

  private Toothpick() {
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

    ScopeNode previousScope;
    ScopeNode lastScope = null;
    for (Object name : names) {
      previousScope = lastScope;
      lastScope = (ScopeNode) openScope(name);
      if (previousScope != null) {
        //if there was already such a node, we add a new child
        //but there might already be such a child, in that case
        //we use it.
        lastScope = previousScope.addChild(lastScope);
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
      //if there was already a scope by this name, we return it
      scope = previous;
    }
    return scope;
  }

  /**
   * Detach a scope from its parent, this will trigger the garbage collection of this scope and it's sub-scopes
   * if they are not referenced outside of Toothpick.
   *
   * @param name the name of the scope to close.
   */

  public static void closeScope(Object name) {
    //we remove the scope first, so that other threads don't see it, and see the next snapshot of the tree
    ScopeNode scope = (ScopeNode) MAP_KEY_TO_SCOPE.remove(name);
    if (scope != null) {
      ScopeNode parentScope = scope.getParentScope();
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
    ScopeImpl.reset();
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

  /**
   * Removes all nodes of {@code scope} using DFS. We don't lock here.
   *
   * @param scope the parent scope of which all children will recursively be removed
   * from the map. We don't do anything else to the children nodes are they will be
   * garbage collected soon. We just cut a whole sub-graph in the references graph of the JVM normally.
   */
  private static void removeScopeAndChildrenFromMap(ScopeNode scope) {
    MAP_KEY_TO_SCOPE.remove(scope.getName());
    for (ScopeNode childScope : scope.childrenScopes.values()) {
      removeScopeAndChildrenFromMap(childScope);
    }
  }

  public static void setConfiguration(Configuration configuration) {
    Configuration.setConfiguration(configuration);
  }

  /*for testing.*/
  static int getScopeNamesSize() {
    return MAP_KEY_TO_SCOPE.size();
  }
}
