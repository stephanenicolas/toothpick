package toothpick.compiler.memberinjector;

import com.google.common.base.Joiner;
import com.google.testing.compile.JavaFileObjects;
import javax.tools.JavaFileObject;
import org.junit.Test;
import toothpick.compiler.factory.BaseFactoryTest;

import static com.google.common.truth.Truth.assert_;
import static com.google.testing.compile.JavaSourceSubjectFactory.javaSource;
import static toothpick.compiler.memberinjector.ProcessorTestUtilities.memberInjectorProcessorsFailingWhenMethodIsNotPackageVisible;

public class RelaxedMemberInjectorWarningsTest extends BaseFactoryTest {
  @Test
  public void testInjectedMethod_shouldFailTheBuild_whenMethodIsPublic() {
    JavaFileObject source = JavaFileObjects.forSourceString("test.TestWarningVisibleInjectedMethod", Joiner.on('\n').join(//
        "package test;", //
        "import javax.inject.Inject;", //
        "import javax.inject.Singleton;", //
        "public class TestWarningVisibleInjectedMethod {", //
        "  @Inject public void init() {}", //
        "}"));

    assert_().about(javaSource())
        .that(source)
        .processedWith(memberInjectorProcessorsFailingWhenMethodIsNotPackageVisible())
        .failsToCompile();
  }

  @Test
  public void testInjectedMethod_shouldNotFailTheBuild_whenMethodIsPublicButAnnotated() {
    JavaFileObject source = JavaFileObjects.forSourceString("test.TestWarningVisibleInjectedMethod", Joiner.on('\n').join(//
        "package test;", //
        "import javax.inject.Inject;", //
        "import javax.inject.Singleton;", //
        "public class TestWarningVisibleInjectedMethod {", //
        "  @SuppressWarnings(\"visible\")", //
        "  @Inject public void init() {}", //
        "}"));

    assert_().about(javaSource())
        .that(source)
        .processedWith(memberInjectorProcessorsFailingWhenMethodIsNotPackageVisible())
        .compilesWithoutError();
  }

  @Test
  public void testInjectedMethod_shouldFailTheBuild_whenMethodIsProtected() {
    JavaFileObject source = JavaFileObjects.forSourceString("test.TestWarningVisibleInjectedMethod", Joiner.on('\n').join(//
        "package test;", //
        "import javax.inject.Inject;", //
        "import javax.inject.Singleton;", //
        "public class TestWarningVisibleInjectedMethod {", //
        "  @Inject protected void init() {}", //
        "}"));

    assert_().about(javaSource())
        .that(source)
        .processedWith(memberInjectorProcessorsFailingWhenMethodIsNotPackageVisible())
        .failsToCompile();
  }

  @Test
  public void testInjectedMethod_shouldNotFailTheBuild_whenMethodIsProtectedButAnnotated() {
    JavaFileObject source = JavaFileObjects.forSourceString("test.TestWarningVisibleInjectedMethod", Joiner.on('\n').join(//
        "package test;", //
        "import javax.inject.Inject;", //
        "import javax.inject.Singleton;", //
        "public class TestWarningVisibleInjectedMethod {", //
        "  @SuppressWarnings(\"visible\")", //
        "  @Inject protected void init() {}", //
        "}"));

    assert_().about(javaSource())
        .that(source)
        .processedWith(memberInjectorProcessorsFailingWhenMethodIsNotPackageVisible())
        .compilesWithoutError();
  }
}
