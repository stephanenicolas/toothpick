package toothpick;

import java.util.HashMap;
import java.util.Map;

/**
 * Main class to access toothpick features.
 * It allows to create / retrieve scopes.
 */
public final class ToothPick {

  private static final Map<Object, Scope> MAP_KEY_TO_SCOPE = new HashMap<>();
  private static Injector injector = new InjectorImpl();

  private ToothPick() {
    throw new RuntimeException("Constructor can't be invoked even via reflection.");
  }

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

  public static Scope openScope(Object name) {
    synchronized (MAP_KEY_TO_SCOPE) {
      Scope scope = MAP_KEY_TO_SCOPE.get(name);
      if (scope == null) {
        scope = new ScopeImpl(name);
        MAP_KEY_TO_SCOPE.put(name, scope);
      }
      return scope;
    }
  }

  public static void closeScope(Object name) {
    synchronized (MAP_KEY_TO_SCOPE) {
      Scope scope = MAP_KEY_TO_SCOPE.get(name);
      if (scope != null) {
        Scope parentScope = scope.getParentScope();
        if (parentScope != null) {
          parentScope.removeChild(scope);
        }
        removeScopeAndChildrenFromMap(scope);
      }
    }
  }

  public static void reset() {
    synchronized (MAP_KEY_TO_SCOPE) {
      MAP_KEY_TO_SCOPE.clear();
    }
  }

  public static void inject(Object obj, Scope scope) {
    injector.inject(obj, scope);
  }

  // Not synchronized, calling method closeScope is
  private static void removeScopeAndChildrenFromMap(Scope scope) {
    MAP_KEY_TO_SCOPE.remove(scope.getName());
    for (Scope childScope : scope.childrenScopes) {
      removeScopeAndChildrenFromMap(childScope);
    }
  }
}
