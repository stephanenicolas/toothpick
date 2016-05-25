package toothpick.concurrency.threads;

import toothpick.NodeUtil;
import toothpick.Scope;
import toothpick.Toothpick;

public class AddNodeThread extends TestableThread {
  static final int ACCEPTANCE_THRESHOLD = 50;
  static int instanceNumber = 0;
  private Object rootScopeName;

  public AddNodeThread(Object rootScopeName) {
    super("AddNodeThread " + instanceNumber++);
    this.rootScopeName = rootScopeName;
  }

  @Override
  public void doRun() {
    //pick a random node in the tree, starting from root
    //add a new child node to this node
    Scope scopeName = NodeUtil.findRandomNode(rootScopeName, ACCEPTANCE_THRESHOLD);
    if (scopeName == null) {
      setIsSuccessful(true);
      return;
    }
    Object name = scopeName.getName();
    Toothpick.openScopes(name, new Object());
    setIsSuccessful(true);
  }
}
