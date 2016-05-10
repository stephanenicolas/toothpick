package toothpick.concurrency.threads;

import java.util.List;
import toothpick.Scope;
import toothpick.ToothPick;
import toothpick.concurrency.utils.ThreadTestUtil;

public class AddScopeToListThread extends TestableThread {
  static final int ACCEPTANCE_THRESHOLD = 50;
  static int instanceNumber = 0;
  private List<Object> scopeNames;

  public AddScopeToListThread(List<Object> scopeNames) {
    super("AddNodeThread " + instanceNumber++);
    this.scopeNames = scopeNames;
  }

  @Override
  public void doRun() {
    //pick a random node in the tree, starting from root
    //add a new child node to this node
    Scope scopeName = ThreadTestUtil.findRandomNode(scopeNames, ACCEPTANCE_THRESHOLD);
    if (scopeName == null) {
      setIsSuccessful(true);
      return;
    }
    Object name = scopeName.getName();
    Object newScopeName = new Object();
    scopeNames.add(newScopeName);
    ToothPick.openScopes(name, newScopeName);
    setIsSuccessful(true);
  }
}
