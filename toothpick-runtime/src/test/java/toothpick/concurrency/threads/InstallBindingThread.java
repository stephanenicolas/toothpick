package toothpick.concurrency.threads;

import java.io.IOException;
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
import toothpick.concurrency.utils.ClassCreator;
import toothpick.config.Module;
import toothpick.data.Foo;
import toothpick.data.IFoo;

public class InstallBindingThread extends TestableThread {
  static final int CLASSES_COUNT = 200;
  static int instanceNumber = 0;
  private Object rootScopeName;
  private static Random random = new Random();
  private static ClassCreator classCreator = new ClassCreator();

  private final static Class[] allClasses = new Class[CLASSES_COUNT];

  static {
    try {
      for (int indexClass = 0; indexClass < CLASSES_COUNT; indexClass++) {
        allClasses[indexClass] = classCreator.createClass("Class_"+indexClass);
      }
    } catch (ClassNotFoundException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

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
