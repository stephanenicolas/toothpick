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
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.api.function.Executable;

class TestScopeName {

  @RegisterExtension ToothPickExtension toothPickExtension = new ToothPickExtension(this, "Foo");

  @RegisterExtension
  ToothPickExtension toothPickRuleWithoutScopeName = new ToothPickExtension(this);

  @Test
  void testSetScopeName_shouldFail_whenScopeNameWasAlreadySet() {
    assertThrows(
        IllegalStateException.class,
        new Executable() {
          @Override
          public void execute() {
            toothPickExtension.setScopeName("Bar");
          }
        });
  }

  @Test
  void testSetScopeName_shouldFail_whenScopeNameAlreadyContainsATestModule() {
    assertThrows(
        IllegalStateException.class,
        new Executable() {
          @Override
          public void execute() {
            toothPickRuleWithoutScopeName.setScopeName("Foo");
          }
        });
  }

  @Test
  void testScopeNameSetByConstruction() {
    assertThat(toothPickExtension.getScope(), notNullValue());
    assertThat(toothPickExtension.getScope().getName(), is((Object) "Foo"));
    assertThat(toothPickExtension.getTestModule(), notNullValue());
  }

  @Test
  void testSetScopeName() {
    toothPickRuleWithoutScopeName.setScopeName("Bar");
    assertThat(toothPickRuleWithoutScopeName.getScope(), notNullValue());
    assertThat(toothPickRuleWithoutScopeName.getScope().getName(), is((Object) "Bar"));
    assertThat(toothPickRuleWithoutScopeName.getTestModule(), notNullValue());
  }
}
