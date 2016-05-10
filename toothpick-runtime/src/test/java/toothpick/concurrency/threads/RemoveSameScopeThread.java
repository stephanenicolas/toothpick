package toothpick.concurrency.threads;

import toothpick.ToothPick;

public class RemoveSameScopeThread extends TestableThread {
  static int instanceNumber = 0;
  private final Object childScopeName;

  public RemoveSameScopeThread(Object childScopeName) {
    super("RemoveNodeThread " + instanceNumber++);
    this.childScopeName = childScopeName;
  }

  @Override
  public void doRun() {
    ToothPick.closeScope(childScopeName);
    setIsSuccessful(true);
  }
}
