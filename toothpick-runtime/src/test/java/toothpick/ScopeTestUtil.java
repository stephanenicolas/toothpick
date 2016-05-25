package toothpick;

import java.util.Random;
import java.util.Stack;

public class ScopeTestUtil {
  private static final Random RANDOM = new Random();
  private static final int RANDOM_INTERVAL_LENGTH = 100;

  private ScopeTestUtil() {
  }

  public static Scope findRandomNode(Object rooScopeName, int acceptanceThreshold) {
    ScopeNode root = (ScopeNode) Toothpick.openScope(rooScopeName);
    ScopeNode result = null;
    Stack<ScopeNode> scopeStack = new Stack<>();
    scopeStack.push(root);
    while (result == null && !scopeStack.isEmpty()) {
      ScopeNode scope = scopeStack.pop();
      if (RANDOM.nextInt(RANDOM_INTERVAL_LENGTH) < acceptanceThreshold && scope != root) {
        result = scope;
      } else {
        for (ScopeNode childScope : scope.getChildrenScopes()) {
          scopeStack.push(childScope);
        }
      }
    }
    return result;
  }
}
