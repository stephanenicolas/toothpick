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

import static org.hamcrest.MatcherAssert.assertThat;

import java.util.regex.Pattern;
import org.hamcrest.text.MatchesPattern;
import org.junit.Test;
import toothpick.config.Module;
import toothpick.data.Bar;
import toothpick.data.Foo;

public class ScopeImplDumpTest {

  @Test
  public void testToString() {
    // GIVEN
    ScopeImpl scope = new ScopeImpl("root");
    scope.installModules(new TestModule1());
    ScopeImpl childScope = new ScopeImpl("child");
    scope.addChild(childScope);

    // WHEN
    childScope.getInstance(Bar.class);
    String dump = scope.toString();

    // THEN
    Pattern expected =
        Pattern.compile(
            "root:\\d+.*"
                + "Providers: \\[toothpick.Scope,toothpick.data.Foo\\].*"
                + "\\\\---child:\\d+.*"
                + "Providers:.*\\[toothpick.Scope\\].*"
                + "UnScoped providers: \\[toothpick.data.Bar\\].*",
            Pattern.DOTALL);
    assertThat(dump, MatchesPattern.matchesPattern(expected));
  }

  private static class TestModule1 extends Module {
    TestModule1() {
      bind(Foo.class).to(Foo.class);
    }
  }
}
