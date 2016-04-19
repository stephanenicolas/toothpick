package toothpick;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Main class to access toothpick features.
 * It allows to create / retrieve scopes.
 */
public final class ToothPick {

  //http://stackoverflow.com/a/29421697/693752
  //it should really be final, if not volatile
  private static final ConcurrentHashMap<Object, Scope> MAP_KEY_TO_INJECTOR = new ConcurrentHashMap<>();
  private static Injector injector = new InjectorImpl();

  private ToothPick() {
    throw new RuntimeException("Constructor can't be invoked even via reflection.");
  }

  public static Scope openScopes(Object... names) {
    if (names == null) {
      throw new IllegalArgumentException("null scopes can't be open.");
    }

    Scope previousScope = null;
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
    Scope scope = MAP_KEY_TO_INJECTOR.get(name);
    if (scope == null) {
      synchronized (MAP_KEY_TO_INJECTOR) {
        scope = MAP_KEY_TO_INJECTOR.get(name);
        if (scope == null) {
          scope = new ScopeImpl(name);
          MAP_KEY_TO_INJECTOR.put(name, scope);
        }
      }
    }
    return scope;
  }

  public static void closeScope(Object key) {
    Scope scope = openScope(key);
    if (scope == null) {
      return;
    }

    MAP_KEY_TO_INJECTOR.remove(key);
    for (Scope childScope : scope.childrenScopes) {
      MAP_KEY_TO_INJECTOR.remove(scope.getName());
    }

    Scope parentScope = scope.getParentScope();
    if (parentScope != null) {
      parentScope.removeChild(scope);
    }
  }

  public static void reset() {
    MAP_KEY_TO_INJECTOR.clear();
  }

  public static void inject(Object obj, Scope scope) {
    injector.inject(obj, scope);
  }

  public static <T> void inject(Class<T> clazz, T obj, Scope scope) {
    injector.inject(clazz, obj, scope);
  }
}
