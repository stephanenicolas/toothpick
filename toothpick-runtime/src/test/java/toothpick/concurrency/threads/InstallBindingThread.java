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
  static int instanceNumber = 0;
  private ClassCreator classCreator;
  private Object rootScopeName;
  private static Random random = new Random();

  public InstallBindingThread(ClassCreator classCreator, Object rootScopeName) {
    super("InstallBindingThread " + instanceNumber++);
    this.classCreator = classCreator;
    this.rootScopeName = rootScopeName;
  }

  @Override
  public void doRun() {
    Scope scope = ToothPick.openScope(rootScopeName);
    scope.installModules(new Module() {
      {
        Class clazz = classCreator.allClasses[random.nextInt(classCreator.allClasses.length)];
        bind(clazz).to(new Foo());
      }
    });
    setIsSuccessful(true);
  }
}
