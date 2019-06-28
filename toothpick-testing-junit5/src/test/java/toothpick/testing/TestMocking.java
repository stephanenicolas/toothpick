/*
 * Copyright 2016 Stephane Nicolas
 * Copyright 2016 Daniel Molinero Reguerra
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

import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsNull.notNullValue;

import javax.inject.Inject;
import org.easymock.Mock;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

class TestMocking {

  @RegisterExtension ToothPickExtension toothPickExtension = new ToothPickExtension(this, "Foo");
  @RegisterExtension EasyMockExtension easyMockExtension = new EasyMockExtension(this);

  @Mock Dependency dependency;

  private EntryPoint entryPoint;

  @Test
  void testMock() {
    // GIVEN
    expect(dependency.num()).andReturn(2);
    replay(dependency);

    // WHEN
    entryPoint = toothPickExtension.getInstance(EntryPoint.class);
    int num = entryPoint.dependency.num();

    // THEN
    verify(dependency);
    assertThat(entryPoint, notNullValue());
    assertThat(entryPoint.dependency, notNullValue());
    assertThat(num, is(2));
  }

  public static class EntryPoint {
    @Inject Dependency dependency;
  }

  public static class Dependency {
    public int num() {
      return 1;
    }
  }
}
