package toothpick.concurrency.threads;

import toothpick.Scope;
import toothpick.ToothPick;
import toothpick.config.Module;
import toothpick.data.Foo;
import toothpick.data.IFoo;

public class InstallBindingThread extends TestableThread {
  static final int ACCEPTANCE_THRESHOLD = 50;
  static int instanceNumber = 0;
  private Object rootScopeName;

  public InstallBindingThread(Object rootScopeName) {
    super("InstallBindingThread " + instanceNumber++);
    this.rootScopeName = rootScopeName;
  }

  @Override
  public void doRun() {
    Scope scope = ToothPick.openScope(rootScopeName);
    scope.installModules(new Module() {
      {
        bind(IFoo.class).to(new Foo());
      }
    });
    setIsSuccessful(true);
  }
}
