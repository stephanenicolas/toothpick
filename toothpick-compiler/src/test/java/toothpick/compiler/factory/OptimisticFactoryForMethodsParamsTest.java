package toothpick.compiler.factory;

import com.google.common.base.Joiner;
import com.google.testing.compile.JavaFileObjects;
import javax.tools.JavaFileObject;
import javax.tools.StandardLocation;
import org.junit.Test;

import static com.google.common.truth.Truth.assert_;
import static com.google.testing.compile.JavaSourceSubjectFactory.javaSource;

public class OptimisticFactoryForMethodsParamsTest extends BaseFactoryTest {
  @Test
  public void testOptimisticFactoryCreationForInjectedMethod() {
    JavaFileObject source = JavaFileObjects.forSourceString("test.TestOptimisticFactoryCreationForInjectedMethod", Joiner.on('\n').join(//
        "package test;", //
        "import javax.inject.Inject;", //
        "import toothpick.ScopeInstances;", //
        "public class TestOptimisticFactoryCreationForInjectedMethod {", //
        "  @Inject void m(Foo foo) {}", //
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
  public void testOptimisticFactoryCreationForInjectedMethod_shouldWorkButNoFactoryIsProduced_whenTypeIsPrivate() {
    JavaFileObject source = JavaFileObjects.forSourceString("test.TestOptimisticFactoryCreationForInjectedMethod", Joiner.on('\n').join(//
        "package test;", //
        "import javax.inject.Inject;", //
        "import toothpick.ScopeInstances;", //
        "public class TestOptimisticFactoryCreationForInjectedMethod {", //
        "  @Inject void m(Foo foo) {}", //
        "  public static class Foo {}", //
        "}"));

    assertThatCompileWithoutErrorButNoFactoryIsNotCreated(source, "test", "Foo");
  }

  @Test
  public void testOptimisticFactoryCreationForInjectedMethod_shouldFail_whenMethodIsInvalid() {
    JavaFileObject source = JavaFileObjects.forSourceString("test.TestOptimisticFactoryCreationForInjectedMethod", Joiner.on('\n').join(//
        "package test;", //
        "import javax.inject.Inject;", //
        "import toothpick.ScopeInstances;", //
        "@ScopeInstances", //
        "public class TestOptimisticFactoryCreationForInjectedMethod {", //
        "  @Inject private void m(Foo foo) {}", //
        "  private static class Foo {}", //
        "}"));

    assert_().about(javaSource())
        .that(source)
        .processedWith(ProcessorTestUtilities.factoryProcessors())
        .failsToCompile()
        .withErrorContaining("@Inject annotated methods must not be private : test.TestOptimisticFactoryCreationForInjectedMethod#m");
  }
}
