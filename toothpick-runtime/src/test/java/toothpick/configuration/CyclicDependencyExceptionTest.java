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

import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.List;
import org.hamcrest.CoreMatchers;
import org.junit.Test;
import toothpick.data.Bar;
import toothpick.data.BarChild;
import toothpick.data.CyclicFoo;
import toothpick.data.Foo;
import toothpick.data.FooProvider;

public class CyclicDependencyExceptionTest {

  @Test
  public void testConstructor_shouldCreateEmptyMessage_whenNotPassedAMessage() {
    // GIVEN

    // WHEN
    CyclicDependencyException exception = new CyclicDependencyException();

    // THEN
    assertThat(exception.getMessage(), nullValue());
  }

  @Test
  public void testConstructor_shouldCreateMessage_whenPassedAMessage() {
    // GIVEN

    // WHEN
    CyclicDependencyException exception = new CyclicDependencyException("Foo");

    // THEN
    assertThat(exception.getMessage(), CoreMatchers.is("Foo"));
  }

  @Test
  public void testConstructor_shouldCreateCause_whenPassedACause() {
    // GIVEN
    Throwable cause = new Exception("Foo");

    // WHEN
    CyclicDependencyException exception = new CyclicDependencyException(cause);

    // THEN
    assertThat(exception.getMessage(), CoreMatchers.is("java.lang.Exception: Foo"));
    assertThat(exception.getCause(), CoreMatchers.is(cause));
  }

  @Test
  public void testConstructor_shouldCreateMessageAndCause_whenPassedAMessageAndCause() {
    // GIVEN
    Throwable cause = new Exception();

    // WHEN
    CyclicDependencyException exception = new CyclicDependencyException("Foo", cause);

    // THEN
    assertThat(exception.getMessage(), CoreMatchers.is("Foo"));
    assertThat(exception.getCause(), CoreMatchers.is(cause));
  }

  @Test
  public void newCyclicDependencyException_showGenerateWholePath_whenCycleStartsPath() {
    String expectedErrorMessage =
        "Class toothpick.data.CyclicFoo creates a cycle:\n"
            + "\n"
            + "               ================\n"
            + "              ||              ||\n"
            + "              \\/              ||\n"
            + "   toothpick.data.CyclicFoo   ||\n"
            + "              ||              ||\n"
            + "      toothpick.data.Bar      ||\n"
            + "              ||              ||\n"
            + "      toothpick.data.Foo      ||\n"
            + "              ||              ||\n"
            + "    toothpick.data.BarChild   ||\n"
            + "              ||              ||\n"
            + "               ================\n";

    // GIVEN
    List<Class<?>> path = new ArrayList<>();
    path.add(CyclicFoo.class);
    path.add(Bar.class);
    path.add(Foo.class);
    path.add(BarChild.class);

    // WHEN
    CyclicDependencyException exception = new CyclicDependencyException(path, CyclicFoo.class);

    // THEN
    assertThat(exception.getMessage(), is(expectedErrorMessage));
  }

  @Test
  public void newCyclicDependencyException_showGeneratePartialPath_whenCycleStartsInTheMiddle() {
    String expectedErrorMessage =
        "Class toothpick.data.CyclicFoo creates a cycle:\n"
            + "\n"
            + "               ================\n"
            + "              ||              ||\n"
            + "              \\/              ||\n"
            + "   toothpick.data.CyclicFoo   ||\n"
            + "              ||              ||\n"
            + "      toothpick.data.Foo      ||\n"
            + "              ||              ||\n"
            + "    toothpick.data.BarChild   ||\n"
            + "              ||              ||\n"
            + "               ================\n";

    // GIVEN
    List<Class<?>> path = new ArrayList<>();
    path.add(Bar.class);
    path.add(CyclicFoo.class);
    path.add(Foo.class);
    path.add(BarChild.class);

    // WHEN
    CyclicDependencyException exception = new CyclicDependencyException(path, CyclicFoo.class);

    // THEN
    assertThat(exception.getMessage(), is(expectedErrorMessage));
  }

  @Test
  public void newCyclicDependencyException_showGenerateLastElementPath_whenCycleFinishesPath() {
    String expectedErrorMessage =
        "Class toothpick.data.CyclicFoo creates a cycle:\n"
            + "\n"
            + "               ================\n"
            + "              ||              ||\n"
            + "              \\/              ||\n"
            + "   toothpick.data.CyclicFoo   ||\n"
            + "              ||              ||\n"
            + "               ================\n";

    // GIVEN
    List<Class<?>> path = new ArrayList<>();
    path.add(Bar.class);
    path.add(Foo.class);
    path.add(BarChild.class);
    path.add(CyclicFoo.class);

    // WHEN
    CyclicDependencyException exception = new CyclicDependencyException(path, CyclicFoo.class);

    // THEN
    assertThat(exception.getMessage(), is(expectedErrorMessage));
  }

  @Test
  public void newCyclicDependencyException_showGenerateWholePath_whenStartCannotBeFound() {
    String expectedErrorMessage =
        "Class toothpick.data.FooProvider creates a cycle:\n"
            + "\n"
            + "               ================\n"
            + "              ||              ||\n"
            + "              \\/              ||\n"
            + "   toothpick.data.CyclicFoo   ||\n"
            + "              ||              ||\n"
            + "      toothpick.data.Bar      ||\n"
            + "              ||              ||\n"
            + "      toothpick.data.Foo      ||\n"
            + "              ||              ||\n"
            + "    toothpick.data.BarChild   ||\n"
            + "              ||              ||\n"
            + "               ================\n";

    // GIVEN
    List<Class<?>> path = new ArrayList<>();
    path.add(CyclicFoo.class);
    path.add(Bar.class);
    path.add(Foo.class);
    path.add(BarChild.class);

    // WHEN
    CyclicDependencyException exception = new CyclicDependencyException(path, FooProvider.class);

    // THEN
    assertThat(exception.getMessage(), is(expectedErrorMessage));
  }

  @Test
  public void
      newCyclicDependencyException_showGenerateOneElementPath_whenCycleContainsOneElement() {
    String expectedErrorMessage =
        "Class toothpick.data.CyclicFoo creates a cycle:\n"
            + "\n"
            + "               ================\n"
            + "              ||              ||\n"
            + "              \\/              ||\n"
            + "   toothpick.data.CyclicFoo   ||\n"
            + "              ||              ||\n"
            + "               ================\n";

    // GIVEN
    List<Class<?>> path = new ArrayList<>();
    path.add(CyclicFoo.class);

    // WHEN
    CyclicDependencyException exception = new CyclicDependencyException(path, CyclicFoo.class);

    // THEN
    assertThat(exception.getMessage(), is(expectedErrorMessage));
  }

  @Test(expected = NullPointerException.class)
  @SuppressWarnings("ThrowableInstanceNeverThrown")
  public void newCyclicDependencyException_showThrowNullPointerException_whenPathIsNull() {
    // GIVEN, WHEN
    new CyclicDependencyException(null, CyclicFoo.class);

    // THEN
    fail("Should throw an exception as a wrong parameters are passed");
  }

  @Test(expected = IllegalArgumentException.class)
  @SuppressWarnings("ThrowableInstanceNeverThrown")
  public void newCyclicDependencyException_showThrowIllegalArgumentException_whenPathIsEmpty() {
    // GIVEN, WHEN
    new CyclicDependencyException(new ArrayList<Class<?>>(), CyclicFoo.class);

    // THEN
    fail("Should throw an exception as a wrong parameters are passed");
  }

  @Test(expected = NullPointerException.class)
  @SuppressWarnings("ThrowableInstanceNeverThrown")
  public void newCyclicDependencyException_showThrowNullPointerException_whenStartClassIsNull() {
    // GIVEN
    List<Class<?>> path = new ArrayList<>();
    path.add(CyclicFoo.class);

    // WHEN
    new CyclicDependencyException(path, null);

    // THEN
    fail("Should throw an exception as a wrong parameters are passed");
  }
}
