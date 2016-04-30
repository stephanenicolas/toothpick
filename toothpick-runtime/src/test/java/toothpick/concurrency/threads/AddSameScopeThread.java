package toothpick.concurrency.threads;

import toothpick.ToothPick;

public class AddSameScopeThread extends TestableThread {
  static int instanceNumber = 0;
  private final Object parentScopeName;
  private final Object childScopeName;

  public AddSameScopeThread(Object parentScopeName, Object childScopeName) {
    super("AddNodeThread " + instanceNumber++);
    this.parentScopeName = parentScopeName;
    this.childScopeName = childScopeName;
  }

  @Override
  public void doRun() {
    ToothPick.openScopes(parentScopeName, childScopeName);
    setIsSuccessful(true);
  }
}
