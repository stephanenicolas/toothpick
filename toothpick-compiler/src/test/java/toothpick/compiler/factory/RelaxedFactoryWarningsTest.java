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

import static com.google.common.truth.Truth.assert_;
import static com.google.testing.compile.JavaSourceSubjectFactory.javaSource;

import com.google.common.base.Joiner;
import com.google.testing.compile.JavaFileObjects;
import javax.tools.JavaFileObject;
import org.junit.Test;

public class RelaxedFactoryWarningsTest extends BaseFactoryTest {
  @Test
  public void
      testOptimisticFactoryCreationForSingleton_shouldFailTheBuild_whenThereIsNoDefaultConstructor() {
    JavaFileObject source =
        JavaFileObjects.forSourceString(
            "test.TestOptimisticFactoryCreationForSingleton",
            Joiner.on('\n')
                .join( //
                    "package test;", //
                    "import javax.inject.Inject;", //
                    "import javax.inject.Singleton;", //
                    "@Singleton", //
                    "public class TestOptimisticFactoryCreationForSingleton {", //
                    "  TestOptimisticFactoryCreationForSingleton(int a) { }", //
                    "}"));

    assert_()
        .about(javaSource())
        .that(source)
        .processedWith(ProcessorTestUtilities.factoryProcessorsFailingOnNonInjectableClasses())
        .failsToCompile();
  }

  @Test
  public void
      testOptimisticFactoryCreationForSingleton_shouldNotFailTheBuild_whenThereIsNoDefaultConstructorButClassIsAnnotated() {
    JavaFileObject source =
        JavaFileObjects.forSourceString(
            "test.TestOptimisticFactoryCreationForSingleton",
            Joiner.on('\n')
                .join( //
                    "package test;", //
                    "import javax.inject.Inject;", //
                    "import javax.inject.Singleton;", //
                    "@SuppressWarnings(\"injectable\")", //
                    "@Singleton", //
                    "public class TestOptimisticFactoryCreationForSingleton {", //
                    "  TestOptimisticFactoryCreationForSingleton(int a) { }", //
                    "}"));

    assert_()
        .about(javaSource())
        .that(source)
        .processedWith(ProcessorTestUtilities.factoryProcessorsFailingOnNonInjectableClasses())
        .compilesWithoutError();
  }

  @Test
  public void
      testOptimisticFactoryCreationWithInjectedMembers_shouldFailTheBuild_whenThereIsNoDefaultConstructor() {
    JavaFileObject source =
        JavaFileObjects.forSourceString(
            "test.TestOptimisticFactoryCreationForSingleton",
            Joiner.on('\n')
                .join( //
                    "package test;", //
                    "import javax.inject.Inject;", //
                    "public class TestOptimisticFactoryCreationForSingleton {", //
                    "  @Inject String s;", //
                    "  TestOptimisticFactoryCreationForSingleton(int a) { }", //
                    "}"));

    assert_()
        .about(javaSource())
        .that(source)
        .processedWith(ProcessorTestUtilities.factoryProcessorsFailingOnNonInjectableClasses())
        .failsToCompile();
  }

  @Test
  public void
      testOptimisticFactoryCreationWithInjectedMembers_shouldNotFailTheBuild_whenThereIsNoDefaultConstructorButClassIsAnnotated() {
    JavaFileObject source =
        JavaFileObjects.forSourceString(
            "test.TestOptimisticFactoryCreationForSingleton",
            Joiner.on('\n')
                .join( //
                    "package test;", //
                    "import javax.inject.Inject;", //
                    "@SuppressWarnings(\"injectable\")", //
                    "public class TestOptimisticFactoryCreationForSingleton {", //
                    "  @Inject String s;", //
                    "  TestOptimisticFactoryCreationForSingleton(int a) { }", //
                    "}"));

    assert_()
        .about(javaSource())
        .that(source)
        .processedWith(ProcessorTestUtilities.factoryProcessorsFailingOnNonInjectableClasses())
        .compilesWithoutError();
  }
}
