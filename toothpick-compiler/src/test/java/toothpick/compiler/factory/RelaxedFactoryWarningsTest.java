package toothpick.compiler.factory;

import com.google.common.base.Joiner;
import com.google.testing.compile.JavaFileObjects;
import javax.tools.JavaFileObject;
import javax.tools.StandardLocation;
import org.junit.Test;

import static com.google.common.truth.Truth.assert_;
import static com.google.testing.compile.JavaSourceSubjectFactory.javaSource;

public class RelaxedFactoryWarningsTest extends BaseFactoryTest {
  @Test
  public void testOptimisticFactoryCreationForSingleton_shouldFailTheBuild_whenThereIsNoDefaultConstructor() {
    JavaFileObject source = JavaFileObjects.forSourceString("test.TestOptimisticFactoryCreationForSingleton", Joiner.on('\n').join(//
        "package test;", //
        "import javax.inject.Inject;", //
        "import javax.inject.Singleton;", //
        "@Singleton", //
        "public class TestOptimisticFactoryCreationForSingleton {", //
        "  TestOptimisticFactoryCreationForSingleton(int a) { }", //
        "}"));

    assert_().about(javaSource())
        .that(source)
        .processedWith(ProcessorTestUtilities.factoryProcessorsFailingOnNonInjectableClasses())
        .failsToCompile();
  }

  @Test
  public void testOptimisticFactoryCreationWithInjectedMembers_shouldFailTheBuild_whenThereIsNoDefaultConstructor() {
    JavaFileObject source = JavaFileObjects.forSourceString("test.TestOptimisticFactoryCreationForSingleton", Joiner.on('\n').join(//
        "package test;", //
        "import javax.inject.Inject;", //
        "public class TestOptimisticFactoryCreationForSingleton {", //
        "  @Inject String s;", //
        "  TestOptimisticFactoryCreationForSingleton(int a) { }", //
        "}"));

    assert_().about(javaSource())
        .that(source)
        .processedWith(ProcessorTestUtilities.factoryProcessorsFailingOnNonInjectableClasses())
        .failsToCompile();
  }
}
