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
package toothpick;

import static org.junit.Assert.fail;

import javax.inject.Provider;
import org.junit.Test;

public class InternalProviderImplTest {

  @Test(expected = IllegalArgumentException.class)
  public void testCreateInternalProviderImpl_shouldFail_whenInstanceIsNull() {
    // GIVEN
    // WHEN
    new InternalProviderImpl((String) null);

    // THEN
    fail("Should throw an exception");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testCreateInternalProviderImpl_shouldFail_whenProviderInstanceIsNull() {
    // GIVEN
    // WHEN
    new InternalProviderImpl((Provider<String>) null, false, false);

    // THEN
    fail("Should throw an exception");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testCreateInternalProviderImpl_shouldFail_whenFactoryIsNull() {
    // GIVEN
    // WHEN
    new InternalProviderImpl((Factory<String>) null);

    // THEN
    fail("Should throw an exception");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testCreateInternalProviderImpl_shouldFail_whenFactoryClassIsNull() {
    // GIVEN
    // WHEN
    new InternalProviderImpl((Class) null, false, false, false, false, false);

    // THEN
    fail("Should throw an exception");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testCreateInternalProviderImpl_shouldFail_whenProviderFactoryClassIsNull() {
    // GIVEN
    // WHEN
    new InternalProviderImpl((Class) null, true, false, false, false, false);

    // THEN
    fail("Should throw an exception");
  }

  /* TODO we should have unit tests for this
  @Test
  public void testGet() throws Exception {

  }
  */
}
