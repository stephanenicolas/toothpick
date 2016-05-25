package toothpick.concurrency.threads;

import java.util.List;
import toothpick.NodeUtil;
import toothpick.Scope;
import toothpick.Toothpick;

public class RemoveScopeFromListThread extends TestableThread {
  static final int ACCEPTANCE_THRESHOLD = 50;
  static int instanceNumber = 0;
  private List<Object> scopeNames;

  public RemoveScopeFromListThread(List<Object> scopeNames) {
    super("RemoveNodeThread " + instanceNumber++);
    this.scopeNames = scopeNames;
  }

  @Override
  public void doRun() {
    //pick a random node in the tree, starting from root
    //add a new child node to this node
    Scope scope = NodeUtil.findRandomNode(scopeNames, ACCEPTANCE_THRESHOLD);
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
