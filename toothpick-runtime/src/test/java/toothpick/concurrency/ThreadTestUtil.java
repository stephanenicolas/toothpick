package toothpick.concurrency;

import java.util.Random;
import java.util.Stack;
import toothpick.Scope;
import toothpick.ToothPick;

public class ThreadTestUtil {//find a child node of root via DSF, take a random child.
  static final Random random = new Random();
  static final int RANDOM_INTERVAL_LENGTH = 100;
  static final int STANDARD_THREAD_COUNT = 5000;

  // can be null
  static Scope findRandomNode(int acceptanceThreshold) {
    Scope root = ToothPick.openScope(MultiThreadTest.ROOT_SCOPE);
    Scope result = null;
    Stack<Scope> scopeStack = new Stack<Scope>();
    scopeStack.push(root);
    while (result == null && !scopeStack.isEmpty()) {
      Scope scope = scopeStack.pop();
      if (random.nextInt(RANDOM_INTERVAL_LENGTH) < acceptanceThreshold) {
        result = scope;
      } else {
        for (Scope childScope : scope.getChildrenScopes()) {
          scopeStack.push(childScope);
        }
      }
    }
    return result;
  }
}