/*
 * Copyright 2019 Stephane Nicolas
 * Copyright 2019 Daniel Molinero Reguera
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package toothpick;

import java.util.List;
import java.util.Random;
import java.util.Stack;

public class NodeUtil {
  private static final Random RANDOM = new Random();
  private static final int RANDOM_INTERVAL_LENGTH = 100;

  private NodeUtil() {}

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
