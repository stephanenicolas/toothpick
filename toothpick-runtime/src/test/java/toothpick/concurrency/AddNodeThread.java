package toothpick.concurrency;

import toothpick.Scope;
import toothpick.ToothPick;

class AddNodeThread extends TestableThread {
  static final int ACCEPTANCE_THRESHOLD = 50;
  static int instanceNumber = 0;

  public AddNodeThread() {
    super("AddNodeThread " + instanceNumber++);
  }

  @Override
  public void doRun() {
    //pick a random node in the tree, starting from root
    //add a new child node to this node
    Scope scopeName = ThreadTestUtil.findRandomNode(ACCEPTANCE_THRESHOLD);
    if (scopeName == null) {
      setIsSuccessful(true);
      return;
    }
    Object name = scopeName.getName();
    ToothPick.openScopes(name, new Object());
    setIsSuccessful(true);
  }
}
