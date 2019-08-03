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
package toothpick.concurrency.threads;

import toothpick.NodeUtil;
import toothpick.Scope;
import toothpick.Toothpick;

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
    // pick a random node in the tree, starting from root
    // add a new child node to this node
    Scope scopeName = NodeUtil.findRandomNode(rootScopeName, ACCEPTANCE_THRESHOLD);
    if (scopeName == null) {
      setIsSuccessful(true);
      return;
    }
    Object name = scopeName.getName();
    Toothpick.openScopes(name, new Object());
    setIsSuccessful(true);
  }
}
