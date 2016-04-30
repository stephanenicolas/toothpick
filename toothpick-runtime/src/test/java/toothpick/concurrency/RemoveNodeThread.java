package toothpick.concurrency;

import toothpick.Scope;
import toothpick.ToothPick;

class RemoveNodeThread extends TestableThread {
  static final int ACCEPTANCE_THRESHOLD = 50;
  static int instanceNumber = 0;

  public RemoveNodeThread() {
    super("RemoveNodeThread " + instanceNumber++);
  }

  @Override
  public void doRun() {
    //pick a random node in the tree, starting from root
    //add a new child node to this node
    Scope scope = ThreadTestUtil.findRandomNode(ACCEPTANCE_THRESHOLD);
    if (scope == null) {
      setIsSuccessful(true);
      return;
    }
    //remove any node except root
    if (scope.getParentScope() == null) {
      setIsSuccessful(true);
      return;
    }
    Object scopeName = scope.getName();
    ToothPick.closeScope(scopeName);
    setIsSuccessful(true);
  }
}
