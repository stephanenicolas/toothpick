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
package toothpick.locators;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.Test;

public class NoFactoryFoundExceptionTest {

  @Test
  public void testConstructor_shouldCreateMessage_whenPassedAClass() throws Exception {
    // GIVEN

    // WHEN
    NoFactoryFoundException exception = new NoFactoryFoundException(String.class);

    // THEN
    assertThat(exception.getMessage(), notNullValue());
  }

  @Test
  public void testConstructor_shouldCreateCauseAndMessage_whenPassedAClassAndACause()
      throws Exception {
    // GIVEN

    // WHEN
    Throwable cause = new Throwable();
    NoFactoryFoundException exception = new NoFactoryFoundException(String.class, cause);

    // THEN
    assertThat(exception.getMessage(), notNullValue());
    assertThat(exception.getCause(), is(cause));
  }
}
