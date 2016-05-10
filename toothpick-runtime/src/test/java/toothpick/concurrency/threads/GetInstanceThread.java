package toothpick.concurrency.threads;

import toothpick.ToothPick;

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
    ToothPick.openScope(scopeName).getInstance(clazz);
    setIsSuccessful(true);
  }
}
