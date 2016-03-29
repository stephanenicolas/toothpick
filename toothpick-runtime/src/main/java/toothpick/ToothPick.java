package toothpick;

import java.util.concurrent.ConcurrentHashMap;
import toothpick.config.Module;

import static java.lang.String.format;

/**
 * Main class to access toothpick features.
 * It allows to create / retrieve injectors.
 */
public final class ToothPick {

  //http://stackoverflow.com/a/29421697/693752
  //it should really be final, if not volatile
  private static final ConcurrentHashMap<Object, Injector> mapKeyToInjector = new ConcurrentHashMap<>();

  //JACOCO:OFF
  private ToothPick() {
  }
  //JACOCO:ON

  public static Injector createInjector(Object key, Module... modules) {
    return createInjector(null, key, modules);
  }

  public static Injector createInjector(Injector parent, Object key, Module... modules) {
    Injector injector = mapKeyToInjector.get(key);
    if (injector != null) {
      throw new IllegalStateException(format("An injector for key %s already exists: %s", key, injector));
    }

    synchronized (mapKeyToInjector) {
      injector = mapKeyToInjector.get(key);
      if (injector != null) {
        throw new IllegalStateException(format("An injector for key %s already exists: %s", key, injector));
      }
      injector = new InjectorImpl(parent, key, modules);
      mapKeyToInjector.put(key, injector);
    }
    return injector;
  }

  public static Injector getInjector(Object key) {
    return mapKeyToInjector.get(key);
  }

  public static Injector getOrCreateInjector(Injector parent, Object key, Module... modules) {
    Injector injector = getInjector(key);
    if (injector == null) {
      synchronized (mapKeyToInjector) {
        if (injector == null) {
          injector = createInjector(parent, key, modules);
        }
      }
    }
    return injector;
  }

  public static void destroyInjector(Object key) {
    Injector injector = getInjector(key);
    if (injector == null) {
      return;
    }
    mapKeyToInjector.remove(key);
  }

  public static void reset() {
    mapKeyToInjector.clear();
  }
}
