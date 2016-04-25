package toothpick.compiler.factory;

import com.google.common.base.Joiner;
import com.google.testing.compile.JavaFileObjects;
import javax.tools.JavaFileObject;
import javax.tools.StandardLocation;
import org.junit.Test;

import static com.google.common.truth.Truth.assert_;
import static com.google.testing.compile.JavaSourceSubjectFactory.javaSource;

public class OptimisticFactoryForFieldsTest extends BaseFactoryTest {
  @Test
  public void testOptimisticFactoryCreationForInjectedField() {
    JavaFileObject source = JavaFileObjects.forSourceString("test.TestOptimisticFactoryCreationForInjectedField", Joiner.on('\n').join(//
        "package test;", //
        "import javax.inject.Inject;", //
        "import toothpick.ScopeInstances;", //
        "public class TestOptimisticFactoryCreationForInjectedField {", //
        "  @Inject Foo foo;", //
        "}", //
        "  class Foo {}"));

    assert_().about(javaSource())
        .that(source)
        .processedWith(ProcessorTestUtilities.factoryAndMemberInjectorProcessors())
        .compilesWithoutError()
        .and()
        .generatesFileNamed(StandardLocation.locationFor("CLASS_OUTPUT"), "test", "Foo$$Factory.class");
  }

  @Test
  public void testOptimisticFactoryCreationForInjectedField_shouldFail_WhenFieldIsInvalid() {
    JavaFileObject source = JavaFileObjects.forSourceString("test.TestOptimisticFactoryCreationForInjectedField", Joiner.on('\n').join(//
        "package test;", //
        "import javax.inject.Inject;", //
        "import toothpick.ScopeInstances;", //
        "public class TestOptimisticFactoryCreationForInjectedField {", //
        "  @Inject private Foo foo;", //
        "}", //
        "  class Foo {}"));

    assert_().about(javaSource())
        .that(source)
        .processedWith(ProcessorTestUtilities.factoryProcessors())
        .failsToCompile()
        .withErrorContaining("@Inject annotated fields must be non private : test.TestOptimisticFactoryCreationForInjectedField#foo");
  }

  @Test
  public void testOptimisticFactoryCreationForInjectedField_shouldWorkButNoFactoryIsProduced_whenTypeIsAbstract() {
    JavaFileObject source = JavaFileObjects.forSourceString("test.TestOptimisticFactoryCreationForInjectedField", Joiner.on('\n').join(//
        "package test;", //
        "import javax.inject.Inject;", //
        "import toothpick.ScopeInstances;", //
        "public class TestOptimisticFactoryCreationForInjectedField {", //
        "  @Inject Foo foo;", //
        "}", //
        "  abstract class Foo {}"));

    assertThatCompileWithoutErrorButNoFactoryIsNotCreated(source, "test", "Foo");
  }

  @Test
  public void testOptimisticFactoryCreationForInjectedField_shouldWorkButNoFactoryIsProduced_whenTypeHasANonDefaultConstructor() {
    JavaFileObject source = JavaFileObjects.forSourceString("test.TestOptimisticFactoryCreationForInjectedField", Joiner.on('\n').join(//
        "package test;", //
        "import javax.inject.Inject;", //
        "import toothpick.ScopeInstances;", //
        "public class TestOptimisticFactoryCreationForInjectedField {", //
        "  @Inject Foo foo;", //
        "}", //
        "class Foo {", //
        " public Foo(String s) {}", //
        "}"));

    assertThatCompileWithoutErrorButNoFactoryIsNotCreated(source, "test", "Foo");
  }

  @Test
  public void testOptimisticFactoryCreationForInjectedField_shouldWorkButNoFactoryIsProduced_whenTypeHasAPrivateDefaultConstructor() {
    JavaFileObject source = JavaFileObjects.forSourceString("test.TestOptimisticFactoryCreationForInjectedField", Joiner.on('\n').join(//
        "package test;", //
        "import javax.inject.Inject;", //
        "import toothpick.ScopeInstances;", //
        "public class TestOptimisticFactoryCreationForInjectedField {", //
        "  @Inject Foo foo;", //
        "}", //
        "class Foo {", //
        " private Foo() {}", //
        "}"));

    assertThatCompileWithoutErrorButNoFactoryIsNotCreated(source, "test", "Foo");
  }
}
