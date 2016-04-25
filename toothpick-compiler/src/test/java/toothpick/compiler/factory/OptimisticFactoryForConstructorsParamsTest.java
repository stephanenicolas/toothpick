package toothpick.compiler.factory;

import com.google.common.base.Joiner;
import com.google.testing.compile.JavaFileObjects;
import javax.tools.JavaFileObject;
import javax.tools.StandardLocation;
import org.junit.Test;

import static com.google.common.truth.Truth.assert_;
import static com.google.testing.compile.JavaSourceSubjectFactory.javaSource;

public class OptimisticFactoryForConstructorsParamsTest extends BaseFactoryTest {
  @Test
  public void testOptimisticFactoryCreationForInjectedConstructorParam() {
    JavaFileObject source = JavaFileObjects.forSourceString("test.TestOptimisticFactoryCreationForInjectedConstructor", Joiner.on('\n').join(//
        "package test;", //
        "import javax.inject.Inject;", //
        "import toothpick.ScopeInstances;", //
        "public class TestOptimisticFactoryCreationForInjectedConstructor {", //
        "  @Inject void TestOptimisticFactoryCreationForInjectedConstructor(Foo foo) {}", //
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
  public void testOptimisticFactoryCreationForInjectedConstructorParam_shouldWorkButNoFactoryIsProduced_whenTypeIsInterface() {
    JavaFileObject source = JavaFileObjects.forSourceString("test.TestOptimisticFactoryCreationForInjectedConstructor", Joiner.on('\n').join(//
        "package test;", //
        "import javax.inject.Inject;", //
        "import toothpick.ScopeInstances;", //
        "public class TestOptimisticFactoryCreationForInjectedConstructor {", //
        "  @Inject void TestOptimisticFactoryCreationForInjectedConstructor(Foo foo) {}", //
        "}", //
        "  interface Foo {}"));

    assertThatCompileWithoutErrorButNoFactoryIsNotCreated(source, "test", "Foo");
  }
}
