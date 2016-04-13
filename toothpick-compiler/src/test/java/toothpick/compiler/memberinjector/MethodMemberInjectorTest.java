package toothpick.compiler.memberscope;

import com.google.common.base.Joiner;
import com.google.testing.compile.JavaFileObjects;
import javax.tools.JavaFileObject;
import org.junit.Test;

import static com.google.common.truth.Truth.assert_;
import static com.google.testing.compile.JavaSourceSubjectFactory.javaSource;
import static toothpick.compiler.memberscope.ProcessorTestUtilities.memberInjectorProcessors;

public class MethodMemberInjectorTest {
  @Test
  public void testSimpleMethodInjection() {
    JavaFileObject source = JavaFileObjects.forSourceString("test.TestMethodInjection", Joiner.on('\n').join(//
        "package test;", //
        "import javax.inject.Inject;", //
        "public class TestMethodInjection {", //
        "  @Inject", //
        "  public void m(Foo foo) {}", //
        "}", //
        "class Foo {}" //
    ));

    JavaFileObject expectedSource = JavaFileObjects.forSourceString("test/TestMethodInjection$$MemberInjector", Joiner.on('\n').join(//
        "package test;", //
        "", //
        "import java.lang.Override;", //
        "import toothpick.MemberInjector;", //
        "import toothpick.Scope;", //
        "", //
        "public final class TestMethodInjection$$MemberInjector implements MemberInjector<TestMethodInjection> {", //
        "  @Override", //
        "  public void inject(TestMethodInjection target, Scope scope) {", //
        "    Foo param1 = scope.getInstance(test.Foo.class);", //
        "    target.m(param1);", //
        "  }", //
        "}" //
    ));

    assert_().about(javaSource())
        .that(source)
        .processedWith(memberInjectorProcessors())
        .compilesWithoutError()
        .and()
        .generatesSources(expectedSource);
  }

  @Test
  public void testMethodInjection_shouldFail_whenInjectedMethodIsPrivate() {
    JavaFileObject source = JavaFileObjects.forSourceString("test.TestMethodInjection", Joiner.on('\n').join(//
        "package test;", //
        "import javax.inject.Inject;", //
        "public class TestMethodInjection {", //
        "  @Inject", //
        "  private void m(Foo foo) {}", //
        "}", //
        "class Foo {}" //
    ));

    assert_().about(javaSource())
        .that(source)
        .processedWith(memberInjectorProcessors())
        .failsToCompile()
        .withErrorContaining("@Inject annotated methods must not be private : test.TestMethodInjection.m");
  }
}
