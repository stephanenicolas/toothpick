package toothpick.concurrency.threads;

import java.util.Random;
import toothpick.ToothPick;

public class ScopeToStringThread extends TestableThread {
  static int instanceNumber = 0;
  private Object scopeName;
  private static Random random = new Random();

  public ScopeToStringThread(Object scopeName) {
    super("ScopeToStringThread " + instanceNumber++);
    this.scopeName = scopeName;
  }

  @Override
  public void doRun() {
    ToothPick.openScope(scopeName).toString();
    setIsSuccessful(true);
  }
}
