package toothpick.compiler.factory;

import com.google.common.base.Joiner;
import com.google.testing.compile.JavaFileObjects;
import javax.tools.JavaFileObject;
import org.junit.Test;

import static com.google.common.truth.Truth.assert_;
import static com.google.testing.compile.JavaSourceSubjectFactory.javaSource;

public class RelaxedFactoryForClassContainingMethodsTest extends BaseFactoryTest {
  @Test
  public void testRelaxedFactoryCreationForInjectedMethod() {
    JavaFileObject source = JavaFileObjects.forSourceString("test.TestRelaxedFactoryCreationForInjectMethod", Joiner.on('\n').join(//
        "package test;", //
        "import javax.inject.Inject;", //
        "import toothpick.ScopeInstances;", //
        "public class TestRelaxedFactoryCreationForInjectMethod {", //
        "  @Inject void m(Foo foo) {}", //
        "}", //
        "  class Foo {}"));

    JavaFileObject expectedSource = JavaFileObjects.forSourceString("test/TestRelaxedFactoryCreationForInjectMethod$$Factory", Joiner.on('\n').join(//
        "package test;", //
        "import java.lang.Override;", //
        "import toothpick.Factory;", //
        "import toothpick.MemberInjector;", //
        "import toothpick.Scope;", //
        "", //
        "public final class TestRelaxedFactoryCreationForInjectMethod$$Factory implements Factory<TestRelaxedFactoryCreationForInjectMethod> {", //
        "  private MemberInjector<TestRelaxedFactoryCreationForInjectMethod> memberInjector"
            + " = new test.TestRelaxedFactoryCreationForInjectMethod$$MemberInjector();", //
        "  @Override", //
        "  public TestRelaxedFactoryCreationForInjectMethod createInstance(Scope scope) {", //
        "    scope = getTargetScope(scope);", //
        "    TestRelaxedFactoryCreationForInjectMethod testRelaxedFactoryCreationForInjectMethod"
            + " = new TestRelaxedFactoryCreationForInjectMethod();", //
        "    memberInjector.inject(testRelaxedFactoryCreationForInjectMethod, scope);", //
        "    return testRelaxedFactoryCreationForInjectMethod;", //
        "  }", //
        "  @Override", //
        "  public Scope getTargetScope(Scope scope) {", //
        "    return scope;", //
        "  }", //
        "  @Override", //
        "  public boolean hasScopeAnnotation() {", //
        "    return false;", //
        "  }", //
        "  @Override", //
        "  public boolean hasScopeInstancesAnnotation() {", //
        "    return false;", //
        "  }", //
        "}" //
    ));

    assert_().about(javaSource())
        .that(source)
        .processedWith(ProcessorTestUtilities.factoryAndMemberInjectorProcessors())
        .compilesWithoutError()
        .and()
        .generatesSources(expectedSource);
  }

  @Test
  public void testRelaxedFactoryCreationForInjectedMethod_shouldFail_WhenMethodIsInvalid() {
    JavaFileObject source = JavaFileObjects.forSourceString("test.TestRelaxedFactoryCreationForInjectMethod", Joiner.on('\n').join(//
        "package test;", //
        "import javax.inject.Inject;", //
        "import toothpick.ScopeInstances;", //
        "public class TestRelaxedFactoryCreationForInjectMethod {", //
        "  @Inject private void m(Foo foo) {}", //
        "}", //
        "  class Foo {}"));

    assert_().about(javaSource())
        .that(source)
        .processedWith(ProcessorTestUtilities.factoryAndMemberInjectorProcessors())
        .failsToCompile()
        .withErrorContaining("@Inject annotated methods must not be private : test.TestRelaxedFactoryCreationForInjectMethod#m");
  }

  @Test
  public void testRelaxedFactoryCreationForInjectedMethod_shouldWorkButNoFactoryIsProduced_whenTypeIsAbstract() {
    JavaFileObject source = JavaFileObjects.forSourceString("test.TestRelaxedFactoryCreationForInjectMethod", Joiner.on('\n').join(//
        "package test;", //
        "import javax.inject.Inject;", //
        "import toothpick.ScopeInstances;", //
        "public abstract class TestRelaxedFactoryCreationForInjectMethod {", //
        "  @Inject void m(Foo foo) {}", //
        "}", //
        "  class Foo {}"));

    assertThatCompileWithoutErrorButNoFactoryIsCreated(source, "test", "TestRelaxedFactoryCreationForInjectMethod");
  }

  @Test
  public void testRelaxedFactoryCreationForInjectedMethod_shouldWorkButNoFactoryIsProduced_whenTypeHasANonDefaultConstructor() {
    JavaFileObject source = JavaFileObjects.forSourceString("test.TestRelaxedFactoryCreationForInjectMethod", Joiner.on('\n').join(//
        "package test;", //
        "import javax.inject.Inject;", //
        "import toothpick.ScopeInstances;", //
        "public class TestRelaxedFactoryCreationForInjectMethod {", //
        "  @Inject void m(Foo foo) {}", //
        "  public TestRelaxedFactoryCreationForInjectMethod(String s) {}", //
        "}", //
        "class Foo {}"));

    assertThatCompileWithoutErrorButNoFactoryIsCreated(source, "test", "TestRelaxedFactoryCreationForInjectMethod");
  }

  @Test
  public void testRelaxedFactoryCreationForInjectedMethod_shouldWorkButNoFactoryIsProduced_whenTypeHasAPrivateDefaultConstructor() {
    JavaFileObject source = JavaFileObjects.forSourceString("test.TestRelaxedFactoryCreationForInjectMethod", Joiner.on('\n').join(//
        "package test;", //
        "import javax.inject.Inject;", //
        "import toothpick.ScopeInstances;", //
        "public class TestRelaxedFactoryCreationForInjectMethod {", //
        "  @Inject void m(Foo foo) {}", //
        "  private TestRelaxedFactoryCreationForInjectMethod() {}", //
        "}", //
        "class Foo {}"));

    assertThatCompileWithoutErrorButNoFactoryIsCreated(source, "test", "TestRelaxedFactoryCreationForInjectMethod");
  }
}
