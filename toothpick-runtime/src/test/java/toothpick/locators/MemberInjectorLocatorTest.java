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

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.Before;
import org.junit.Test;
import toothpick.MemberInjector;
import toothpick.Toothpick;
import toothpick.configuration.Configuration;
import toothpick.data.Foo;
import toothpick.data.Qurtz;

public class MemberInjectorLocatorTest {

  @Before
  public void setUp() {
    Toothpick.setConfiguration(Configuration.forProduction());
  }

  @Test
  public void
      testGetMemberInjector_shouldFindTheMemberInjectorForFoo_whenTheMemberInjectorIsGenerated() {
    // GIVEN
    // WHEN
    MemberInjector<Foo> memberInjector = MemberInjectorLocator.getMemberInjector(Foo.class);

    // THEN
    assertThat(memberInjector, notNullValue());
  }

  @Test
  public void testGetMemberInjector_shouldNotReturnNull_whenTheMemberInjectorIsNotGenerated() {
    // GIVEN
    // WHEN
    MemberInjector<Qurtz> memberInjector = MemberInjectorLocator.getMemberInjector(Qurtz.class);

    // THEN
    assertThat(memberInjector, nullValue());
  }
}
