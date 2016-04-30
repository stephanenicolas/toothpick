package toothpick.concurrency.threads;

import java.lang.reflect.GenericDeclaration;
import java.lang.reflect.Proxy;
import java.security.Key;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.jar.JarEntry;
import javax.inject.Inject;
import toothpick.Lazy;
import toothpick.Scope;
import toothpick.ToothPick;
import toothpick.config.Module;
import toothpick.data.Foo;
import toothpick.data.IFoo;

public class InstallBindingThread extends TestableThread {
  static final int ACCEPTANCE_THRESHOLD = 50;
  static int instanceNumber = 0;
  private Object rootScopeName;
  private static Random random = new Random();

  private final static Class[] allClasses = new Class[] {
    String.class, Inject.class, Integer.class, Float.class, Objects.class, Enumeration.class,
      Double.class, Date.class, GenericDeclaration.class, HashMap.class, JarEntry.class, Key.class, Lazy.class,
      Long.class, Map.class, Math.class
  };

  public InstallBindingThread(Object rootScopeName) {
    super("InstallBindingThread " + instanceNumber++);
    this.rootScopeName = rootScopeName;
  }

  @Override
  public void doRun() {
    Scope scope = ToothPick.openScope(rootScopeName);
    scope.installModules(new Module() {
      {
        Class clazz = allClasses[random.nextInt(allClasses.length)];
        bind(clazz).to(new Foo());
      }
    });
    setIsSuccessful(true);
  }
}
