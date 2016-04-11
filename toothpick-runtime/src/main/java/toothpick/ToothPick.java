package toothpick;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Main class to access toothpick features.
 * It allows to create / retrieve injectors.
 */
public final class ToothPick {

  //http://stackoverflow.com/a/29421697/693752
  //it should really be final, if not volatile
  private static final ConcurrentHashMap<Object, Injector> MAP_KEY_TO_INJECTOR = new ConcurrentHashMap<>();

  private ToothPick() {
    throw new RuntimeException("Constructor can't be invoked even via reflection.");
  }

  public static Injector openInjector(Object key) {
    Injector injector = MAP_KEY_TO_INJECTOR.get(key);
    if (injector == null) {
      synchronized (MAP_KEY_TO_INJECTOR) {
        injector = MAP_KEY_TO_INJECTOR.get(key);
        if (injector == null) {
          injector = new InjectorImpl(key);
          MAP_KEY_TO_INJECTOR.put(key, injector);
        }
      }
    }
    return injector;
  }

  public static void closeInjector(Object key) {
    Injector injector = openInjector(key);
    if (injector == null) {
      return;
    }

    MAP_KEY_TO_INJECTOR.remove(key);
    for (Injector childInjector : injector.childrenInjector) {
      MAP_KEY_TO_INJECTOR.remove(childInjector.getName());
    }

    Injector parentInjector = injector.getParentInjector();
    if (parentInjector != null) {
      parentInjector.removeChildInjector(injector);
    }
  }

  public static void reset() {
    MAP_KEY_TO_INJECTOR.clear();
  }
}
