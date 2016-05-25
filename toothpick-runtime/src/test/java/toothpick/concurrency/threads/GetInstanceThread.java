package toothpick.concurrency.threads;

import toothpick.Toothpick;

public class GetInstanceThread extends TestableThread {
  static int instanceNumber = 0;
  private Object scopeName;
  private Class clazz;

  public GetInstanceThread(Object scopeName, Class clazz) {
    super("GetInstanceThread " + instanceNumber++);
    this.scopeName = scopeName;
    this.clazz = clazz;
  }

  @Override
  public void doRun() {
    Toothpick.openScope(scopeName).getInstance(clazz);
    setIsSuccessful(true);
  }
}
