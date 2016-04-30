package toothpick.concurrency.threads;

import toothpick.Scope;
import toothpick.ToothPick;
import toothpick.concurrency.utils.ThreadTestUtil;

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
    Scope scopeName = ThreadTestUtil.findRandomNode(rootScopeName, ACCEPTANCE_THRESHOLD);
    if (scopeName == null) {
      setIsSuccessful(true);
      return;
    }
    Object name = scopeName.getName();
    Scope scope = ToothPick.openScopes(name, new Object());
    if(scope.getParentScope() == null) {
      throw  new RuntimeException("A child scope has no parent !");
    }
    setIsSuccessful(true);
  }
}
