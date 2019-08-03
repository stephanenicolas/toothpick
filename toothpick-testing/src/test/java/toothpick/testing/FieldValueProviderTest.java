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
import static org.junit.Assert.fail;

import java.lang.reflect.Field;
import org.junit.Test;

public class FieldValueProviderTest {

  @Test(expected = RuntimeException.class)
  public void testGet_shouldFail_whenFieldIsNotAccessible() throws NoSuchFieldException {
    // GIVEN
    Foo foo = new Foo();
    Field fieldS = Foo.class.getDeclaredField("s");
    FieldValueProvider fieldValueProviderUnderTest = new FieldValueProvider(fieldS, foo);
    fieldS.setAccessible(false);

    // WHEN
    fieldValueProviderUnderTest.get();

    // THEN
    fail("Should thrown an exception");
  }

  @Test
  public void testGet_shouldReturnFieldValue() throws NoSuchFieldException {
    // GIVEN
    Foo foo = new Foo();
    Field fieldS = Foo.class.getDeclaredField("s");
    FieldValueProvider fieldValueProviderUnderTest = new FieldValueProvider(fieldS, foo);

    // WHEN
    String s = (String) fieldValueProviderUnderTest.get();

    // THEN
    assertThat(s, is(foo.s));
  }

  public static class Foo {
    private String s = "foo";
  }
}
