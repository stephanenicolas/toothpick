package toothpick.compiler.factory;

import com.google.common.base.Joiner;
import com.google.testing.compile.JavaFileObjects;
import javax.tools.JavaFileObject;
import javax.tools.StandardLocation;
import org.junit.Test;

import static com.google.common.truth.Truth.assert_;
import static com.google.testing.compile.JavaSourceSubjectFactory.javaSource;

public class RelaxedFactoryForSingletonsTest extends BaseFactoryTest {
  @Test
  public void testOptimisticFactoryCreationForSingleton() {
    JavaFileObject source = JavaFileObjects.forSourceString("test.TestOptimisticFactoryCreationForSingleton", Joiner.on('\n').join(//
        "package test;", //
        "import javax.inject.Inject;", //
        "import javax.inject.Singleton;", //
        "@Singleton", //
        "public class TestOptimisticFactoryCreationForSingleton {", //
        "}"));

    assert_().about(javaSource())
        .that(source)
        .processedWith(ProcessorTestUtilities.factoryAndMemberInjectorProcessors())
        .compilesWithoutError()
        .and()
        .generatesFileNamed(StandardLocation.locationFor("CLASS_OUTPUT"), "test", "TestOptimisticFactoryCreationForSingleton$$Factory.class");
  }

  @Test
  public void testOptimisticFactoryCreationForScopeAnnotation() {
    JavaFileObject source = JavaFileObjects.forSourceString("test.TestOptimisticFactoryCreationForScopeAnnotation", Joiner.on('\n').join(//
        "package test;", //
        "import javax.inject.Inject;", //
        "import javax.inject.Scope;", //
        "import java.lang.annotation.Retention;", //
        "import java.lang.annotation.RetentionPolicy;", //
        "@Scope", //
        "@Retention(RetentionPolicy.RUNTIME)", //
        "@interface CustomScope {}", //
        "@CustomScope", //
        "public class TestOptimisticFactoryCreationForScopeAnnotation {", //
        "}"));

    assert_().about(javaSource())
        .that(source)
        .processedWith(ProcessorTestUtilities.factoryProcessorsWithAdditionalTypes("test.CustomScope"))
        .compilesWithoutError()
        .and()
        .generatesFileNamed(StandardLocation.locationFor("CLASS_OUTPUT"), "test", "TestOptimisticFactoryCreationForScopeAnnotation$$Factory.class");
  }

  @Test
  public void testOptimisticFactoryCreationForScopeAnnotation_shouldFail_WhenScopeAnnotationDoesNotHaveRetention() {
    JavaFileObject source = JavaFileObjects.forSourceString("test.TestOptimisticFactoryCreationForScopeAnnotation", Joiner.on('\n').join(//
        "package test;", //
        "import javax.inject.Inject;", //
        "import javax.inject.Scope;", //
        "import java.lang.annotation.Retention;", //
        "import java.lang.annotation.RetentionPolicy;", //
        "@Scope", //
        "@interface CustomScope {}", //
        "@CustomScope", //
        "public class TestOptimisticFactoryCreationForScopeAnnotation {", //
        "}"));

    assert_().about(javaSource())
        .that(source)
        .processedWith(ProcessorTestUtilities.factoryProcessorsWithAdditionalTypes("test.CustomScope"))
        .failsToCompile();
  }

  @Test
  public void testOptimisticFactoryCreationForScopeAnnotation_shouldFail_WhenScopeAnnotationDoesNotHaveRuntimeRetention() {
    JavaFileObject source = JavaFileObjects.forSourceString("test.TestOptimisticFactoryCreationForScopeAnnotation", Joiner.on('\n').join(//
        "package test;", //
        "import javax.inject.Inject;", //
        "import javax.inject.Scope;", //
        "import java.lang.annotation.Retention;", //
        "import java.lang.annotation.RetentionPolicy;", //
        "@Scope", //
        "@Retention(RetentionPolicy.CLASS)", //
        "@interface CustomScope {}", //
        "@CustomScope", //
        "public class TestOptimisticFactoryCreationForScopeAnnotation {", //
        "}"));

    assert_().about(javaSource())
        .that(source)
        .processedWith(ProcessorTestUtilities.factoryProcessorsWithAdditionalTypes("test.CustomScope"))
        .failsToCompile();
  }
}
