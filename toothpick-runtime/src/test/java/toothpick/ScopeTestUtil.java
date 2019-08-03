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

import java.util.Random;
import java.util.Stack;

public class ScopeTestUtil {
  private static final Random RANDOM = new Random();
  private static final int RANDOM_INTERVAL_LENGTH = 100;

  private ScopeTestUtil() {}

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
