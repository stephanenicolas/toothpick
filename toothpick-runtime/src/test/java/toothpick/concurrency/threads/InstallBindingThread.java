package toothpick.concurrency.threads;

import java.util.Random;
import toothpick.Scope;
import toothpick.Toothpick;
import toothpick.concurrency.utils.ClassCreator;
import toothpick.config.Module;
import toothpick.data.Foo;

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
    Scope scope = Toothpick.openScope(rootScopeName);
    scope.installModules(new Module() {
      {
        Class clazz = classCreator.allClasses[random.nextInt(classCreator.allClasses.length)];
        bind(clazz).toInstance(new Foo());
      }
    });
    setIsSuccessful(true);
  }
}
