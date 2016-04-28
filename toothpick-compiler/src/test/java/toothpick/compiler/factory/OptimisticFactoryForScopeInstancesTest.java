package toothpick.compiler.factory;

import com.google.common.base.Joiner;
import com.google.testing.compile.JavaFileObjects;
import javax.tools.JavaFileObject;
import javax.tools.StandardLocation;
import org.junit.Test;

import static com.google.common.truth.Truth.assert_;
import static com.google.testing.compile.JavaSourceSubjectFactory.javaSource;

public class OptimisticFactoryForScopeInstancesTest extends BaseFactoryTest {
  @Test
  public void testOptimisticFactoryCreationForHasScopeInstances_shouldFail_whenThereIsNoScopeAnnotation() {
    JavaFileObject source = JavaFileObjects.forSourceString("test.TestOptimisticFactoryCreationForHasScopeInstances", Joiner.on('\n').join(//
        "package test;", //
        "import javax.inject.Inject;", //
        "import toothpick.ScopeInstances;", //
        "@ScopeInstances", //
        "public class TestOptimisticFactoryCreationForHasScopeInstances {", //
        "}"));

    assert_().about(javaSource())
        .that(source)
        .processedWith(ProcessorTestUtilities.factoryAndMemberInjectorProcessors())
        .failsToCompile()
        .withErrorContaining(
            "The type test.TestOptimisticFactoryCreationForHasScopeInstances" + " uses @ScopeInstances but doesn't have a scope annotation.");
  }

  @Test
  public void testOptimisticFactoryCreationForHasScopeInstances_shouldWork_whenThereIsAScopeAnnotation() {
    JavaFileObject source = JavaFileObjects.forSourceString("test.TestOptimisticFactoryCreationForHasScopeInstances", Joiner.on('\n').join(//
        "package test;", //
        "import javax.inject.Inject;", //
        "import javax.inject.Scope;", //
        "import toothpick.ScopeInstances;", //
        "@Scope", //
        "@interface CustomScope {}", //
        "@ScopeInstances @CustomScope", //
        "public class TestOptimisticFactoryCreationForHasScopeInstances {", //
        "}"));

    assert_().about(javaSource())
        .that(source)
        .processedWith(ProcessorTestUtilities.factoryAndMemberInjectorProcessors())
        .compilesWithoutError()
        .and()
        .generatesFileNamed(StandardLocation.locationFor("CLASS_OUTPUT"), "test", "TestOptimisticFactoryCreationForHasScopeInstances$$Factory.class");
  }
}
