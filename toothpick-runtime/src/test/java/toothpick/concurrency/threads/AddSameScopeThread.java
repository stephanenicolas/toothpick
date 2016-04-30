package toothpick.concurrency.threads;

import toothpick.Scope;
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
    Scope scope = ToothPick.openScopes(parentScopeName, childScopeName);
    if(scope.getParentScope() == null) {
      throw  new RuntimeException();
    }
    setIsSuccessful(true);
  }
}
