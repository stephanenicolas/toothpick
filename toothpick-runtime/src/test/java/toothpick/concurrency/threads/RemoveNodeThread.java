package toothpick.concurrency.threads;

import toothpick.NodeUtil;
import toothpick.Scope;
import toothpick.Toothpick;

public class RemoveNodeThread extends TestableThread {
  static final int ACCEPTANCE_THRESHOLD = 50;
  static int instanceNumber = 0;
  private Object rootScopeName;

  public RemoveNodeThread(Object rootScopeName) {
    super("RemoveNodeThread " + instanceNumber++);
    this.rootScopeName = rootScopeName;
  }

  @Override
  public void doRun() {
    //pick a random node in the tree, starting from root
    //add a new child node to this node
    Scope scope = NodeUtil.findRandomNode(rootScopeName, ACCEPTANCE_THRESHOLD);
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
    Toothpick.closeScope(scopeName);
    setIsSuccessful(true);
  }
}
