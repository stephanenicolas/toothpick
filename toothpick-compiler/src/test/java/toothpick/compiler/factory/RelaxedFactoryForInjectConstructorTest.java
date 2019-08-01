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

public class RelaxedFactoryForInjectConstructorTest extends BaseFactoryTest {

  @Test
  public void
      testOptimisticFactoryCreationForInjectConstructor_shouldWork_whenDefaultConstructorIsPresent() {
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

  @Test
  public void
      testOptimisticFactoryCreationForInjectConstructor_shouldUse_uniqueConstructorWhenAnnotated() {
    JavaFileObject source =
        JavaFileObjects.forSourceString(
            "test.TestNonEmptyConstructor",
            Joiner.on('\n')
                .join( //
                    "package test;", //
                    "import toothpick.InjectConstructor;", //
                    "import toothpick.Lazy;", //
                    "@InjectConstructor", //
                    "public class TestNonEmptyConstructor {", //
                    "  public TestNonEmptyConstructor(Lazy<String> str, Integer n) {}", //
                    "}" //
                    ));

    JavaFileObject expectedSource =
        JavaFileObjects.forSourceString(
            "test/TestNonEmptyConstructor__Factory",
            Joiner.on('\n')
                .join( //
                    "package test;", //
                    "import java.lang.Integer;", //
                    "import java.lang.Override;", //
                    "import java.lang.String;", //
                    "import toothpick.Factory;", //
                    "import toothpick.Lazy;", //
                    "import toothpick.Scope;", //
                    "", //
                    "public final class TestNonEmptyConstructor__Factory implements Factory<TestNonEmptyConstructor> {", //
                    "  @Override", //
                    "  public TestNonEmptyConstructor createInstance(Scope scope) {", //
                    "    scope = getTargetScope(scope);", //
                    "    Lazy<String> param1 = scope.getLazy(String.class);", //
                    "    Integer param2 = scope.getInstance(Integer.class);", //
                    "    TestNonEmptyConstructor testNonEmptyConstructor = new TestNonEmptyConstructor(param1, param2);", //
                    "    return testNonEmptyConstructor;", //
                    "  }", //
                    "  @Override", //
                    "  public Scope getTargetScope(Scope scope) {", //
                    "    return scope;", //
                    "  }", //
                    "  @Override", //
                    "  public boolean hasScopeAnnotation() {", //
                    "    return false;", //
                    "  }", //
                    "  @Override", //
                    "  public boolean hasSingletonAnnotation() {", //
                    "    return false;", //
                    "  }", //
                    "  @Override", //
                    "  public boolean hasReleasableAnnotation() {", //
                    "    return false;", //
                    "  }", //
                    "  @Override", //
                    "  public boolean hasProvidesSingletonAnnotation() {", //
                    "    return false;", //
                    "  }", //
                    "  @Override", //
                    "  public boolean hasProvidesReleasableAnnotation() {", //
                    "    return false;", //
                    "  }", //
                    "}" //
                    ));

    assert_()
        .about(javaSource())
        .that(source)
        .processedWith(ProcessorTestUtilities.factoryAndMemberInjectorProcessors())
        .compilesWithoutError()
        .and()
        .generatesSources(expectedSource);
  }

  @Test
  public void
      testOptimisticFactoryCreationForInjectConstructor_shouldHave_referenceToMemberInjector() {
    JavaFileObject source =
        JavaFileObjects.forSourceString(
            "test.TestNonEmptyConstructor",
            Joiner.on('\n')
                .join( //
                    "package test;", //
                    "import toothpick.InjectConstructor;", //
                    "import javax.inject.Inject;", //
                    "import toothpick.Lazy;", //
                    "@InjectConstructor", //
                    "public class TestNonEmptyConstructor {", //
                    "  @Inject String string;", //
                    "  public TestNonEmptyConstructor(Lazy<String> str, Integer n) {}", //
                    "}" //
                    ));

    JavaFileObject expectedSource =
        JavaFileObjects.forSourceString(
            "test/TestNonEmptyConstructor__Factory",
            Joiner.on('\n')
                .join( //
                    "package test;", //
                    "import java.lang.Integer;", //
                    "import java.lang.Override;", //
                    "import java.lang.String;", //
                    "import toothpick.Factory;", //
                    "import toothpick.Lazy;", //
                    "import toothpick.MemberInjector;", //
                    "import toothpick.Scope;", //
                    "", //
                    "public final class TestNonEmptyConstructor__Factory implements Factory<TestNonEmptyConstructor> {", //
                    "  private MemberInjector<TestNonEmptyConstructor> memberInjector = new test.TestNonEmptyConstructor__MemberInjector();", //
                    "", //
                    "  @Override", //
                    "  public TestNonEmptyConstructor createInstance(Scope scope) {", //
                    "    scope = getTargetScope(scope);", //
                    "    Lazy<String> param1 = scope.getLazy(String.class);", //
                    "    Integer param2 = scope.getInstance(Integer.class);", //
                    "    TestNonEmptyConstructor testNonEmptyConstructor = new TestNonEmptyConstructor(param1, param2);", //
                    "    memberInjector.inject(testNonEmptyConstructor, scope);", //
                    "    return testNonEmptyConstructor;", //
                    "  }", //
                    "  @Override", //
                    "  public Scope getTargetScope(Scope scope) {", //
                    "    return scope;", //
                    "  }", //
                    "  @Override", //
                    "  public boolean hasScopeAnnotation() {", //
                    "    return false;", //
                    "  }", //
                    "  @Override", //
                    "  public boolean hasSingletonAnnotation() {", //
                    "    return false;", //
                    "  }", //
                    "  @Override", //
                    "  public boolean hasReleasableAnnotation() {", //
                    "    return false;", //
                    "  }", //
                    "  @Override", //
                    "  public boolean hasProvidesSingletonAnnotation() {", //
                    "    return false;", //
                    "  }", //
                    "  @Override", //
                    "  public boolean hasProvidesReleasableAnnotation() {", //
                    "    return false;", //
                    "  }", //
                    "}" //
                    ));

    assert_()
        .about(javaSource())
        .that(source)
        .processedWith(ProcessorTestUtilities.factoryAndMemberInjectorProcessors())
        .compilesWithoutError()
        .and()
        .generatesSources(expectedSource);
  }

  @Test
  public void
      testOptimisticFactoryCreationForInjectConstructor_shouldFail_uniqueConstructorIsAnnotated() {
    JavaFileObject source =
        JavaFileObjects.forSourceString(
            "test.TestNonEmptyConstructorInjected",
            Joiner.on('\n')
                .join( //
                    "package test;", //
                    "import javax.inject.Inject;", //
                    "import toothpick.InjectConstructor;", //
                    "import toothpick.Lazy;", //
                    "@InjectConstructor", //
                    "public class TestNonEmptyConstructorInjected {", //
                    "  @Inject public TestNonEmptyConstructorInjected(Lazy<String> str, Integer n) {}", //
                    "}" //
                    ));

    assert_()
        .about(javaSource())
        .that(source)
        .processedWith(ProcessorTestUtilities.factoryAndMemberInjectorProcessors())
        .failsToCompile()
        .withErrorContaining(
            "Class test.TestNonEmptyConstructorInjected is annotated with @InjectInjectConstructor. "
                + "Therefore, It must have one unique constructor and it should not be annotated with @Inject.");
  }

  @Test
  public void testOptimisticFactoryCreationForInjectConstructor_shouldFail_multipleConstructors() {
    JavaFileObject source =
        JavaFileObjects.forSourceString(
            "test.TestMultipleConstructors",
            Joiner.on('\n')
                .join( //
                    "package test;", //
                    "import javax.inject.Inject;", //
                    "import toothpick.InjectConstructor;", //
                    "import toothpick.Lazy;", //
                    "@InjectConstructor", //
                    "public class TestMultipleConstructors {", //
                    "  public TestMultipleConstructors(Lazy<String> str, Integer n) {}", //
                    "  public TestMultipleConstructors() {}", //
                    "}" //
                    ));

    assert_()
        .about(javaSource())
        .that(source)
        .processedWith(ProcessorTestUtilities.factoryAndMemberInjectorProcessors())
        .failsToCompile()
        .withErrorContaining(
            "Class test.TestMultipleConstructors is annotated with @InjectInjectConstructor. "
                + "Therefore, It must have one unique constructor and it should not be annotated with @Inject.");
  }
}
