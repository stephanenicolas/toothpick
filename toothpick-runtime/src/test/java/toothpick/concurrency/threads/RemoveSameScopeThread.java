package toothpick.concurrency.threads;

import toothpick.Toothpick;

public class RemoveSameScopeThread extends TestableThread {
  static int instanceNumber = 0;
  private final Object childScopeName;

  public RemoveSameScopeThread(Object childScopeName) {
    super("RemoveNodeThread " + instanceNumber++);
    this.childScopeName = childScopeName;
  }

  @Override
  public void doRun() {
    Toothpick.closeScope(childScopeName);
    setIsSuccessful(true);
  }
}
