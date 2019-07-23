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
package toothpick.testing;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.RegisterExtension;

class SimpleTest {

  private static boolean wasRun = false;

  @RegisterExtension
  ToothPickExtension toothPickExtension =
      new ToothPickExtension(this) {
        @Override
        public void afterEach(ExtensionContext context) throws Exception {
          super.afterEach(context);
          wasRun = true;
        }
      };

  @Test
  void test() {
    // THEN
    assertThat(wasRun, is(false));
  }

  @AfterAll
  static void tearDownAll() {
    // THEN
    assertThat(wasRun, is(true));
  }
}
