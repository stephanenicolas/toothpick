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
import javax.tools.StandardLocation;
import org.junit.Test;

public class RelaxedFactoryForScopeInstancesTest extends BaseFactoryTest {
  @Test
  public void
      testOptimisticFactoryCreationForHasScopeInstances_shouldFail_whenThereIsNoScopeAnnotation() {
    JavaFileObject source =
        JavaFileObjects.forSourceString(
            "test.TestOptimisticFactoryCreationForHasScopeInstances",
            Joiner.on('\n')
                .join( //
                    "package test;", //
                    "import javax.inject.Inject;", //
                    "import toothpick.ProvidesSingleton;", //
                    "@ProvidesSingleton", //
                    "public class TestOptimisticFactoryCreationForHasScopeInstances {", //
                    "}"));

    assert_()
        .about(javaSource())
        .that(source)
        .processedWith(ProcessorTestUtilities.factoryAndMemberInjectorProcessors())
        .failsToCompile()
        .withErrorContaining(
            "The type test.TestOptimisticFactoryCreationForHasScopeInstances"
                + " uses @ProvidesSingleton but doesn't have a scope annotation.");
  }

  @Test
  public void
      testOptimisticFactoryCreationForHasScopeInstances_shouldWork_whenThereIsAScopeAnnotation() {
    JavaFileObject source =
        JavaFileObjects.forSourceString(
            "test.TestOptimisticFactoryCreationForHasScopeInstances",
            Joiner.on('\n')
                .join( //
                    "package test;", //
                    "import javax.inject.Inject;", //
                    "import javax.inject.Scope;", //
                    "import java.lang.annotation.Retention;", //
                    "import java.lang.annotation.RetentionPolicy;", //
                    "import toothpick.ProvidesSingleton;", //
                    "@Scope", //
                    "@Retention(RetentionPolicy.RUNTIME)", //
                    "@interface CustomScope {}", //
                    "@ProvidesSingleton @CustomScope", //
                    "public class TestOptimisticFactoryCreationForHasScopeInstances {", //
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
            "TestOptimisticFactoryCreationForHasScopeInstances__Factory.class");
  }

  @Test
  public void
      testOptimisticFactoryCreationForHasScopeInstances_shouldFail_whenThereIsAScopeAnnotationWithWrongRetention() {
    JavaFileObject source =
        JavaFileObjects.forSourceString(
            "test.TestOptimisticFactoryCreationForHasScopeInstances",
            Joiner.on('\n')
                .join( //
                    "package test;", //
                    "import javax.inject.Inject;", //
                    "import javax.inject.Scope;", //
                    "import java.lang.annotation.Retention;", //
                    "import java.lang.annotation.RetentionPolicy;", //
                    "import toothpick.ProvidesSingleton;", //
                    "@Scope", //
                    "@Retention(RetentionPolicy.CLASS)", //
                    "@interface CustomScope {}", //
                    "@ProvidesSingleton @CustomScope", //
                    "public class TestOptimisticFactoryCreationForHasScopeInstances {", //
                    "}"));

    assert_()
        .about(javaSource())
        .that(source)
        .processedWith(ProcessorTestUtilities.factoryAndMemberInjectorProcessors())
        .failsToCompile();
  }
}
