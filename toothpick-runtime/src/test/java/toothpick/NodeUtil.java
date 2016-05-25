package toothpick;

import java.util.List;
import java.util.Random;
import java.util.Stack;

public class NodeUtil {
  private static final Random RANDOM = new Random();
  private static final int RANDOM_INTERVAL_LENGTH = 100;

  private NodeUtil() {
  }

  public static Scope findRandomNode(Object rooScopeName, int acceptanceThreshold) {
    Scope root = Toothpick.openScope(rooScopeName);
    Scope result = null;
    Stack<Scope> scopeStack = new Stack<Scope>();
    scopeStack.push(root);
    while (result == null && !scopeStack.isEmpty()) {
      Scope scope = scopeStack.pop();
      if (RANDOM.nextInt(RANDOM_INTERVAL_LENGTH) < acceptanceThreshold && scope != root) {
        result = scope;
      } else {
        for (Scope childScope : ((ScopeNode) scope).getChildrenScopes()) {
          scopeStack.push(childScope);
        }
      }
    }
    return result;
  }

  public static Scope findRandomNode(List<Object> scopeNames, int acceptanceThreshold) {
    if (RANDOM.nextInt(RANDOM_INTERVAL_LENGTH) < acceptanceThreshold) {
      return null;
    } else {
      synchronized (scopeNames) {
        if (scopeNames.isEmpty()) {
          return null;
        }
        int position = RANDOM.nextInt(scopeNames.size());
        return Toothpick.openScope(scopeNames.get(position));
      }
    }
  }
}
