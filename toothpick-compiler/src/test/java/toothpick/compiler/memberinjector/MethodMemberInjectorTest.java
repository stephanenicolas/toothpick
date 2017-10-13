package toothpick.compiler.memberinjector;

import com.google.common.base.Joiner;
import com.google.testing.compile.JavaFileObjects;
import javax.tools.JavaFileObject;
import org.junit.Test;

import static com.google.common.truth.Truth.assert_;
import static com.google.testing.compile.JavaSourceSubjectFactory.javaSource;
import static toothpick.compiler.memberinjector.ProcessorTestUtilities.memberInjectorProcessors;

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
        "    Foo param1 = scope.getInstance(Foo.class);", //
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
  public void testSimpleMethodInjectionWithLazy() {
    JavaFileObject source = JavaFileObjects.forSourceString("test.TestMethodInjection", Joiner.on('\n').join(//
        "package test;", //
        "import javax.inject.Inject;", //
        "import toothpick.Lazy;", //
        "public class TestMethodInjection {", //
        "  @Inject", //
        "  public void m(Lazy<Foo> foo) {}", //
        "}", //
        "class Foo {}" //
    ));

    JavaFileObject expectedSource = JavaFileObjects.forSourceString("test/TestMethodInjection$$MemberInjector", Joiner.on('\n').join(//
        "package test;", //
        "", //
        "import java.lang.Override;", //
        "import toothpick.Lazy;", //
        "import toothpick.MemberInjector;", //
        "import toothpick.Scope;", //
        "", //
        "public final class TestMethodInjection$$MemberInjector implements MemberInjector<TestMethodInjection> {", //
        "  @Override", //
        "  public void inject(TestMethodInjection target, Scope scope) {", //
        "    Lazy<Foo> param1 = scope.getLazy(Foo.class);", //
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
  public void testSimpleMethodInjectionWithProvider() {
    JavaFileObject source = JavaFileObjects.forSourceString("test.TestMethodInjection", Joiner.on('\n').join(//
        "package test;", //
        "import javax.inject.Inject;", //
        "import javax.inject.Provider;", //
        "public class TestMethodInjection {", //
        "  @Inject", //
        "  public void m(Provider<Foo> foo) {}", //
        "}", //
        "class Foo {}" //
    ));

    JavaFileObject expectedSource = JavaFileObjects.forSourceString("test/TestMethodInjection$$MemberInjector", Joiner.on('\n').join(//
        "package test;", //
        "", //
        "import java.lang.Override;", //
        "import javax.inject.Provider;", //
        "import toothpick.MemberInjector;", //
        "import toothpick.Scope;", //
        "", //
        "public final class TestMethodInjection$$MemberInjector implements MemberInjector<TestMethodInjection> {", //
        "  @Override", //
        "  public void inject(TestMethodInjection target, Scope scope) {", //
        "    Provider<Foo> param1 = scope.getProvider(Foo.class);", //
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
  public void testSimpleMethodInjectionWithLazyOfGenericTypeButNotLazyOfGenericType() {
    JavaFileObject source = JavaFileObjects.forSourceString("test.TestMethodInjection", Joiner.on('\n').join(//
        "package test;", //
        "import javax.inject.Inject;", //
        "import toothpick.Lazy;", //
        "public class TestMethodInjection {", //
        "  @Inject", //
        "  public void m(Lazy<Foo> foo) {}", //
        "}", //
        "class Foo<T> {}" //
    ));

    JavaFileObject expectedSource = JavaFileObjects.forSourceString("test/TestMethodInjection$$MemberInjector", Joiner.on('\n').join(//
        "package test;", //
        "", //
        "import java.lang.Override;", //
        "import toothpick.Lazy;", //
        "import toothpick.MemberInjector;", //
        "import toothpick.Scope;", //
        "", //
        "public final class TestMethodInjection$$MemberInjector implements MemberInjector<TestMethodInjection> {", //
        "  @Override", //
        "  public void inject(TestMethodInjection target, Scope scope) {", //
        "    Lazy<Foo> param1 = scope.getLazy(Foo.class);", //
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
  public void testSimpleMethodInjectionWithLazyOfGenericType_shouldFail_WithLazyOfGenericType() {
    JavaFileObject source = JavaFileObjects.forSourceString("test.TestMethodInjection", Joiner.on('\n').join(//
        "package test;", //
        "import javax.inject.Inject;", //
        "import toothpick.Lazy;", //
        "public class TestMethodInjection {", //
        "  @Inject", //
        "  public void m(Lazy<Foo<String>> foo) {}", //
        "}", //
        "class Foo<T> {}" //
    ));

    assert_().about(javaSource())
        .that(source)
        .processedWith(memberInjectorProcessors())
        .failsToCompile()
        .withErrorContaining("Lazy/Provider foo is not a valid in m. Lazy/Provider cannot be used on generic types.");
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
        .withErrorContaining("@Inject annotated methods must not be private : test.TestMethodInjection#m");
  }

  @Test
  public void testMethodInjection_shouldFail_whenContainingClassIsPrivate() {
    JavaFileObject source = JavaFileObjects.forSourceString("test.TestMethodInjection", Joiner.on('\n').join(//
        "package test;", //
        "import javax.inject.Inject;", //
        "public class TestMethodInjection {", //
        "  private static class InnerClass {", //
        "    @Inject", //
        "    public void m(Foo foo) {}", //
        "  }", //
        "}", //
        "class Foo {}" //
    ));

    assert_().about(javaSource())
        .that(source)
        .processedWith(memberInjectorProcessors())
        .failsToCompile()
        .withErrorContaining("@Injected fields in class InnerClass. The class must be non private.");
  }

  @Test
  public void testMethodInjection_shouldFail_whenInjectedMethodParameterIsInvalidLazy() {
    JavaFileObject source = JavaFileObjects.forSourceString("test.TestMethodInjection", Joiner.on('\n').join(//
        "package test;", //
        "import javax.inject.Inject;", //
        "import toothpick.Lazy;", //
        "public class TestMethodInjection {", //
        "  @Inject", //
        "  public void m(Lazy foo) {}", //
        "}", //
        "class Foo {}" //
    ));

    assert_().about(javaSource())
        .that(source)
        .processedWith(memberInjectorProcessors())
        .failsToCompile()
        .withErrorContaining("Parameter foo in method/constructor test.TestMethodInjection#m is not a valid toothpick.Lazy.");
  }

  @Test
  public void testMethodInjection_shouldFail_whenInjectedMethodParameterIsInvalidProvider() {
    JavaFileObject source = JavaFileObjects.forSourceString("test.TestMethodInjection", Joiner.on('\n').join(//
        "package test;", //
        "import javax.inject.Inject;", //
        "import javax.inject.Provider;", //
        "public class TestMethodInjection {", //
        "  @Inject", //
        "  public void m(Provider foo) {}", //
        "}", //
        "class Foo {}" //
    ));

    assert_().about(javaSource())
        .that(source)
        .processedWith(memberInjectorProcessors())
        .failsToCompile()
        .withErrorContaining("Parameter foo in method/constructor test.TestMethodInjection#m is not a valid javax.inject.Provider.");
  }

  @Test
  public void testOverrideMethodInjection() {
    JavaFileObject source = JavaFileObjects.forSourceString("test.TestMethodInjectionParent", Joiner.on('\n').join(//
        "package test;", //
        "import javax.inject.Inject;", //
        "public class TestMethodInjectionParent {", //
        "  @Inject", //
        "  public void m(Foo foo) {}", //
        "  public static class TestMethodInjection extends TestMethodInjectionParent {", //
        "    @Inject", //
        "    public void m(Foo foo) {}", //
        "  }", //
        "}", //
        "class Foo {}" //
    ));

    JavaFileObject expectedSource =
        JavaFileObjects.forSourceString("test/TestMethodInjectionParent$TestMethodInjection$$MemberInjector", Joiner.on('\n').join(//
            "package test;", //
            "", //
            "import java.lang.Override;", //
            "import toothpick.MemberInjector;", //
            "import toothpick.Scope;", //
            "", //
            "public final class TestMethodInjectionParent$TestMethodInjection$$MemberInjector "
                + "implements MemberInjector<TestMethodInjectionParent.TestMethodInjection> {",
            //
            "  private MemberInjector superMemberInjector = new test.TestMethodInjectionParent$$MemberInjector();\n", //
            "  @Override", //
            "  public void inject(TestMethodInjectionParent.TestMethodInjection target, Scope scope) {", //
            "    superMemberInjector.inject(target, scope);", //
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
}
