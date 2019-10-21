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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import javax.inject.Inject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.mockito.Mock;

class TestMocking {

  @RegisterExtension ToothPickExtension toothPickExtension = new ToothPickExtension(this, "Foo");
  @RegisterExtension MockitoExtension mockitoExtension = new MockitoExtension(this);

  @Mock Dependency dependency;

  private EntryPoint entryPoint;

  @Test
  void testMock() {
    // GIVEN
    when(dependency.num()).thenReturn(2);

    // WHEN
    entryPoint = toothPickExtension.getInstance(EntryPoint.class);
    int num = entryPoint.dependency.num();

    // THEN
    verify(dependency).num();
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
