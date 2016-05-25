package toothpick.concurrency.threads;

import java.util.List;
import toothpick.NodeUtil;
import toothpick.Scope;
import toothpick.Toothpick;

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
    Scope scopeName = NodeUtil.findRandomNode(scopeNames, ACCEPTANCE_THRESHOLD);
    if (scopeName == null) {
      setIsSuccessful(true);
      return;
    }
    Object name = scopeName.getName();
    Object newScopeName = new Object();
    scopeNames.add(newScopeName);
    Toothpick.openScopes(name, newScopeName);
    setIsSuccessful(true);
  }
}
