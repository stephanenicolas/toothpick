package toothpick.compiler.factory;

import com.google.common.base.Joiner;
import com.google.testing.compile.JavaFileObjects;
import javax.tools.JavaFileObject;
import javax.tools.StandardLocation;
import org.junit.Test;

import static com.google.common.truth.Truth.assert_;
import static com.google.testing.compile.JavaSourceSubjectFactory.javaSource;

public class RelaxedFactoryForScopeInstancesTest extends BaseFactoryTest {
  @Test
  public void testOptimisticFactoryCreationForHasScopeInstances_shouldFail_whenThereIsNoScopeAnnotation() {
    JavaFileObject source = JavaFileObjects.forSourceString("test.TestOptimisticFactoryCreationForHasScopeInstances", Joiner.on('\n').join(//
        "package test;", //
        "import javax.inject.Inject;", //
        "import toothpick.ProvidesSingletonInScope;", //
        "@ProvidesSingletonInScope", //
        "public class TestOptimisticFactoryCreationForHasScopeInstances {", //
        "}"));

    assert_().about(javaSource())
        .that(source)
        .processedWith(ProcessorTestUtilities.factoryAndMemberInjectorProcessors())
        .failsToCompile()
        .withErrorContaining(
            "The type test.TestOptimisticFactoryCreationForHasScopeInstances" + " uses @ProvidesSingletonInScope but doesn't have a scope annotation.");
  }

  @Test
  public void testOptimisticFactoryCreationForHasScopeInstances_shouldWork_whenThereIsAScopeAnnotation() {
    JavaFileObject source = JavaFileObjects.forSourceString("test.TestOptimisticFactoryCreationForHasScopeInstances", Joiner.on('\n').join(//
        "package test;", //
        "import javax.inject.Inject;", //
        "import javax.inject.Scope;", //
        "import java.lang.annotation.Retention;", //
        "import java.lang.annotation.RetentionPolicy;", //
        "import toothpick.ProvidesSingletonInScope;", //
        "@Scope", //
        "@Retention(RetentionPolicy.RUNTIME)", //
        "@interface CustomScope {}", //
        "@ProvidesSingletonInScope @CustomScope", //
        "public class TestOptimisticFactoryCreationForHasScopeInstances {", //
        "}"));

    assert_().about(javaSource())
        .that(source)
        .processedWith(ProcessorTestUtilities.factoryAndMemberInjectorProcessors())
        .compilesWithoutError()
        .and()
        .generatesFileNamed(StandardLocation.locationFor("CLASS_OUTPUT"), "test", "TestOptimisticFactoryCreationForHasScopeInstances$$Factory.class");
  }

  @Test
  public void testOptimisticFactoryCreationForHasScopeInstances_shouldFail_whenThereIsAScopeAnnotationWithWrongRetention() {
    JavaFileObject source = JavaFileObjects.forSourceString("test.TestOptimisticFactoryCreationForHasScopeInstances", Joiner.on('\n').join(//
        "package test;", //
        "import javax.inject.Inject;", //
        "import javax.inject.Scope;", //
        "import java.lang.annotation.Retention;", //
        "import java.lang.annotation.RetentionPolicy;", //
        "import toothpick.ProvidesSingletonInScope;", //
        "@Scope", //
        "@Retention(RetentionPolicy.CLASS)", //
        "@interface CustomScope {}", //
        "@ProvidesSingletonInScope @CustomScope", //
        "public class TestOptimisticFactoryCreationForHasScopeInstances {", //
        "}"));

    assert_().about(javaSource())
        .that(source)
        .processedWith(ProcessorTestUtilities.factoryAndMemberInjectorProcessors())
        .failsToCompile();
  }
}
