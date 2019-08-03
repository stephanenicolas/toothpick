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
package toothpick.configuration;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.Test;

public class IllegalBindingExceptionTest {

  @Test
  public void testConstructor_shouldCreateEmptyMessage_whenNotPassedAMessage() throws Exception {
    // GIVEN

    // WHEN
    IllegalBindingException exception = new IllegalBindingException();

    // THEN
    assertThat(exception.getMessage(), nullValue());
  }

  @Test
  public void testConstructor_shouldCreateMessage_whenPassedAMessage() throws Exception {
    // GIVEN

    // WHEN
    IllegalBindingException exception = new IllegalBindingException("Foo");

    // THEN
    assertThat(exception.getMessage(), is("Foo"));
  }

  @Test
  public void testConstructor_shouldCreateCause_whenPassedACause() throws Exception {
    // GIVEN
    Throwable cause = new Exception("Foo");

    // WHEN
    IllegalBindingException exception = new IllegalBindingException(cause);

    // THEN
    assertThat(exception.getMessage(), is("java.lang.Exception: Foo"));
    assertThat(exception.getCause(), is(cause));
  }

  @Test
  public void testConstructor_shouldCreateMessageAndCause_whenPassedAMessageAndCause()
      throws Exception {
    // GIVEN
    Throwable cause = new Exception();

    // WHEN
    IllegalBindingException exception = new IllegalBindingException("Foo", cause);

    // THEN
    assertThat(exception.getMessage(), is("Foo"));
    assertThat(exception.getCause(), is(cause));
  }
}
