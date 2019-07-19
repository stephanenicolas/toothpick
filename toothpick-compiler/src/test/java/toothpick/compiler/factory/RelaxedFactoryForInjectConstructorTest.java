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
package toothpick.compiler.factory;

import com.google.common.base.Joiner;
import com.google.testing.compile.JavaFileObjects;
import javax.tools.JavaFileObject;
import javax.tools.StandardLocation;
import org.junit.Test;

import static com.google.common.truth.Truth.assert_;
import static com.google.testing.compile.JavaSourceSubjectFactory.javaSource;

public class RelaxedFactoryForInjectConstructorTest extends BaseFactoryTest {

  @Test
  public void
      testOptimisticFactoryCreationForInjectConstructor_shouldWork_whenThereInjectConstructorIsPresent() {
    JavaFileObject source =
        JavaFileObjects.forSourceString(
            "test.TestOptimisticFactoryCreationForInjectConstructor",
            Joiner.on('\n')
                .join( //
                    "package test;", //
                    "import toothpick.InjectConstructor;", //
                    "@InjectConstructor", //
                    "public class TestOptimisticFactoryCreationForInjectConstructor {", //
                    "}"));

    assert_()
        .about(javaSource())
        .that(source)
        .processedWith(ProcessorTestUtilities.factoryAndMemberInjectorProcessors())
        .compilesWithoutError()
        .and()
        .generatesFileNamed(
            StandardLocation.locationFor("CLASS_OUTPUT"),
            "test",
            "TestOptimisticFactoryCreationForInjectConstructor__Factory.class");
  }
}
