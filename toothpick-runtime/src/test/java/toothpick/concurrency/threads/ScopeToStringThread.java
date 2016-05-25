package toothpick.concurrency.threads;

import java.util.Random;
import toothpick.Toothpick;

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
    Toothpick.openScope(scopeName).toString();
    setIsSuccessful(true);
  }
}
