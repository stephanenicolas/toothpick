package toothpick.compiler.memberinjector;

import com.google.common.base.Joiner;
import com.google.testing.compile.JavaFileObjects;
import javax.tools.JavaFileObject;
import org.junit.Test;

import static com.google.common.truth.Truth.assert_;
import static com.google.testing.compile.JavaSourceSubjectFactory.javaSource;
import static toothpick.compiler.memberinjector.ProcessorTestUtilities.memberInjectorProcessors;

public class FieldMemberInjectorTest {
  @Test
  public void testSimpleFieldInjection() {
    JavaFileObject source = JavaFileObjects.forSourceString("test.TestFieldInjection", Joiner.on('\n').join(//
        "package test;", //
        "import javax.inject.Inject;", //
        "public class TestFieldInjection {", //
        "  @Inject Foo foo;", //
        "  public TestFieldInjection() {}", //
        "}", //
        "class Foo {}" //
    ));

    JavaFileObject expectedSource = JavaFileObjects.forSourceString("test/TestFieldInjection$$MemberInjector", Joiner.on('\n').join(//
        "package test;", //
        "", //
        "import java.lang.Override;", //
        "import toothpick.Injector;", //
        "import toothpick.MemberInjector;", //
        "", //
        "public final class TestFieldInjection$$MemberInjector implements MemberInjector<TestFieldInjection> {", //
        "  @Override", //
        "  public void inject(TestFieldInjection target, Injector injector) {", //
        "    target.foo = injector.getInstance(Foo.class);", //
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
  public void testProviderFieldInjection() {
    JavaFileObject source = JavaFileObjects.forSourceString("test.TestFieldInjection", Joiner.on('\n').join(//
        "package test;", //
        "import javax.inject.Inject;", //
        "import javax.inject.Provider;", //
        "public class TestFieldInjection {", //
        "  @Inject Provider<Foo> foo;", //
        "  public TestFieldInjection() {}", //
        "}", //
        "class Foo {}" //
    ));

    JavaFileObject expectedSource = JavaFileObjects.forSourceString("test/TestFieldInjection$$MemberInjector", Joiner.on('\n').join(//
        "package test;", //
        "", //
        "import java.lang.Override;", //
        "import toothpick.Injector;", //
        "import toothpick.MemberInjector;", //
        "", //
        "public final class TestFieldInjection$$MemberInjector implements MemberInjector<TestFieldInjection> {", //
        "  @Override", //
        "  public void inject(TestFieldInjection target, Injector injector) {", //
        "    target.foo = injector.getProvider(Foo.class);", //
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
  public void testLazyFieldInjection() {
    JavaFileObject source = JavaFileObjects.forSourceString("test.TestFieldInjection", Joiner.on('\n').join(//
        "package test;", //
        "import javax.inject.Inject;", //
        "import toothpick.Lazy;", //
        "public class TestFieldInjection {", //
        "  @Inject Lazy<Foo> foo;", //
        "  public TestFieldInjection() {}", //
        "}", //
        "class Foo {}" //
    ));

    JavaFileObject expectedSource = JavaFileObjects.forSourceString("test/TestFieldInjection$$MemberInjector", Joiner.on('\n').join(//
        "package test;", //
        "", //
        "import java.lang.Override;", //
        "import toothpick.Injector;", //
        "import toothpick.MemberInjector;", //
        "", //
        "public final class TestFieldInjection$$MemberInjector implements MemberInjector<TestFieldInjection> {", //
        "  @Override", //
        "  public void inject(TestFieldInjection target, Injector injector) {", //
        "    target.foo = injector.getLazy(Foo.class);", //
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
  public void testFutureFieldInjection() {
    JavaFileObject source = JavaFileObjects.forSourceString("test.TestFieldInjection", Joiner.on('\n').join(//
        "package test;", //
        "import javax.inject.Inject;", //
        "import java.util.concurrent.Future;", //
        "public class TestFieldInjection {", //
        "  @Inject Future<Foo> foo;", //
        "  public TestFieldInjection() {}", //
        "}", //
        "class Foo {}" //
    ));

    JavaFileObject expectedSource = JavaFileObjects.forSourceString("test/TestFieldInjection$$MemberInjector", Joiner.on('\n').join(//
        "package test;", //
        "", //
        "import java.lang.Override;", //
        "import toothpick.Injector;", //
        "import toothpick.MemberInjector;", //
        "", //
        "public final class TestFieldInjection$$MemberInjector implements MemberInjector<TestFieldInjection> {", //
        "  @Override", //
        "  public void inject(TestFieldInjection target, Injector injector) {", //
        "    target.foo = injector.getFuture(Foo.class);", //
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
  public void testFieldInjection_shouldProduceMemberInjector_whenClassHas2Fields() {
    JavaFileObject source = JavaFileObjects.forSourceString("test.TestFieldInjection", Joiner.on('\n').join(//
        "package test;", //
        "import javax.inject.Inject;", //
        "public class TestFieldInjection {", //
        "  @Inject Foo foo;", //
        "  @Inject Foo foo2;", //
        "  public TestFieldInjection() {}", //
        "}", //
        "class Foo {}" //
    ));

    JavaFileObject expectedSource = JavaFileObjects.forSourceString("test/TestFieldInjection$$MemberInjector", Joiner.on('\n').join(//
        "package test;", //
        "", //
        "import java.lang.Override;", //
        "import toothpick.Injector;", //
        "import toothpick.MemberInjector;", //
        "", //
        "public final class TestFieldInjection$$MemberInjector implements MemberInjector<TestFieldInjection> {", //
        "  @Override", //
        "  public void inject(TestFieldInjection target, Injector injector) {", //
        "    target.foo = injector.getInstance(Foo.class);", //
        "    target.foo2 = injector.getInstance(Foo.class);", //
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
  public void testFieldInjection_shouldFail_whenFieldIsPrivate() {
    JavaFileObject source = JavaFileObjects.forSourceString("test.TestFieldInjection", Joiner.on('\n').join(//
        "package test;", //
        "import javax.inject.Inject;", //
        "public class TestFieldInjection {", //
        "  @Inject private Foo foo;", //
        "  public TestFieldInjection() {}", //
        "}", //
        "class Foo {}" //
    ));

    assert_().about(javaSource())
        .that(source)
        .processedWith(memberInjectorProcessors())
        .failsToCompile()
        .withErrorContaining("@Inject annotated fields must be non private : test.TestFieldInjection.foo");
  }

  @Test
  public void testMemberInjection_shouldInjectAsAnInstanceOfSuperClass_whenSuperClassHasInjectedMembers() {
    JavaFileObject source = JavaFileObjects.forSourceString("test.TestMemberInjection", Joiner.on('\n').join(//
        "package test;", //
        "import javax.inject.Inject;", //
        "public class TestMemberInjection extends TestMemberInjectionParent {", //
        "  @Inject Foo foo;", //
        "}", //
        "class TestMemberInjectionParent {", //
        "  @Inject Foo foo;", //
        "}", //
        "class Foo {}" //
    ));

    JavaFileObject expectedSource = JavaFileObjects.forSourceString("test/TestMemberInjection$$MemberInjector", Joiner.on('\n').join(//
        "package test;", //
        "", //
        "import java.lang.Override;", //
        "import toothpick.Injector;", //
        "import toothpick.MemberInjector;", //
        "", //
        "public final class TestMemberInjection$$MemberInjector implements MemberInjector<TestMemberInjection> {", //
        "  private MemberInjector<TestMemberInjectionParent> superMemberInjector " + "= new test.TestMemberInjectionParent$$MemberInjector();",
        //
        "  @Override", //
        "  public void inject(TestMemberInjection target, Injector injector) {", //
        "    target.foo = injector.getInstance(Foo.class);", //
        "    superMemberInjector.inject(target, injector);", //
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
