package toothpick.compiler.factory;

import com.google.common.base.Joiner;
import com.google.testing.compile.JavaFileObjects;
import javax.tools.JavaFileObject;
import org.junit.Test;

import static com.google.common.truth.Truth.assert_;
import static com.google.testing.compile.JavaSourceSubjectFactory.javaSource;

public class FactoryTest extends BaseFactoryTest {
  @Test
  public void testEmptyConstructor() {
    JavaFileObject source = JavaFileObjects.forSourceString("test.TestEmptyConstructor", Joiner.on('\n').join(//
        "package test;", //
        "import javax.inject.Inject;", //
        "public class TestEmptyConstructor {", //
        "  @Inject public TestEmptyConstructor() {}", //
        "}" //
    ));

    JavaFileObject expectedSource = JavaFileObjects.forSourceString("test/TestEmptyConstructor$$Factory", Joiner.on('\n').join(//
        "package test;", //
        "import java.lang.Override;", //
        "import toothpick.Factory;", //
        "import toothpick.Scope;", //
        "", //
        "public final class TestEmptyConstructor$$Factory implements Factory<TestEmptyConstructor> {", //
        "  @Override", //
        "  public TestEmptyConstructor createInstance(Scope scope) {", //
        "    scope = getTargetScope(scope);", //
        "    TestEmptyConstructor testEmptyConstructor = new TestEmptyConstructor();", //
        "    return testEmptyConstructor;", //
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
        .processedWith(ProcessorTestUtilities.factoryProcessors())
        .compilesWithoutError()
        .and()
        .generatesSources(expectedSource);
  }

  @Test
  public void testPrivateConstructor() {
    JavaFileObject source = JavaFileObjects.forSourceString("test.TestPrivateConstructor", Joiner.on('\n').join(//
        "package test;", //
        "import javax.inject.Inject;", //
        "public class TestPrivateConstructor {", //
        "  @Inject private TestPrivateConstructor() {}", //
        "}" //
    ));

    assert_().about(javaSource())
        .that(source)
        .processedWith(ProcessorTestUtilities.factoryProcessors())
        .failsToCompile()
        .withErrorContaining("@Inject constructors must not be private in class test.TestPrivateConstructor");
  }

  @Test
  public void testInjectedConstructorInPrivateClass() {
    JavaFileObject source = JavaFileObjects.forSourceString("test.TestConstructorInPrivateClass", Joiner.on('\n').join(//
        "package test;", //
        "import javax.inject.Inject;", //
        "class TestConstructorInPrivateClass {", //
        "  @Inject public TestConstructorInPrivateClass() {}", //
        "}" //
    ));

    assert_().about(javaSource())
        .that(source)
        .processedWith(ProcessorTestUtilities.factoryProcessors())
        .failsToCompile()
        .withErrorContaining("Class test.TestConstructorInPrivateClass is private. @Inject constructors are not allowed in non public classes.");
  }

  @Test
  public void test2InjectedConstructors() {
    JavaFileObject source = JavaFileObjects.forSourceString("test.TestPrivateConstructor", Joiner.on('\n').join(//
        "package test;", //
        "import javax.inject.Inject;", //
        "public class TestPrivateConstructor {", //
        "  @Inject private TestPrivateConstructor() {}", //
        "  @Inject private TestPrivateConstructor(String s) {}", //
        "}" //
    ));

    assert_().about(javaSource())
        .that(source)
        .processedWith(ProcessorTestUtilities.factoryProcessors())
        .failsToCompile()
        .withErrorContaining("Class test.TestPrivateConstructor cannot have more than one @Inject annotated constructor.");
  }

  @Test
  public void test2Constructors_butOnlyOneIsInjected() {
    JavaFileObject source = JavaFileObjects.forSourceString("test.Test2Constructors", Joiner.on('\n').join(//
        "package test;", //
        "import javax.inject.Inject;", //
        "public class Test2Constructors {", //
        "  @Inject public Test2Constructors() {}", //
        "  public Test2Constructors(String s) {}", //
        "}" //
    ));

    JavaFileObject expectedSource = JavaFileObjects.forSourceString("test/Test2Constructors$$Factory", Joiner.on('\n').join(//
        "package test;", //
        "import java.lang.Override;", //
        "import toothpick.Factory;", //
        "import toothpick.Scope;", //
        "", //
        "public final class Test2Constructors$$Factory implements Factory<Test2Constructors> {", //
        "  @Override", //
        "  public Test2Constructors createInstance(Scope scope) {", //
        "    scope = getTargetScope(scope);", //
        "    Test2Constructors test2Constructors = new Test2Constructors();", //
        "    return test2Constructors;", //
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
        .processedWith(ProcessorTestUtilities.factoryProcessors())
        .compilesWithoutError()
        .and()
        .generatesSources(expectedSource);
  }

  @Test
  public void testAClassThatNeedsInjection_shouldHaveAFactoryThatInjectsIt_whenItHasAnInjectedField() {
    JavaFileObject source = JavaFileObjects.forSourceString("test.TestAClassThatNeedsInjection", Joiner.on('\n').join(//
        "package test;", //
        "import javax.inject.Inject;", //
        "public class TestAClassThatNeedsInjection {", //
        "@Inject String s;", //
        "  @Inject public TestAClassThatNeedsInjection() {}", //
        "}" //
    ));

    JavaFileObject expectedSource = JavaFileObjects.forSourceString("test/TestAClassThatNeedsInjection$$Factory", Joiner.on('\n').join(//
        "package test;", //
        "import java.lang.Override;", //
        "import toothpick.Factory;", //
        "import toothpick.MemberInjector;", //
        "import toothpick.Scope;", //
        "", //
        "public final class TestAClassThatNeedsInjection$$Factory implements Factory<TestAClassThatNeedsInjection> {", //
        "  private MemberInjector<TestAClassThatNeedsInjection> memberInjector = new test.TestAClassThatNeedsInjection$$MemberInjector();", //
        "  @Override", //
        "  public TestAClassThatNeedsInjection createInstance(Scope scope) {", //
        "    scope = getTargetScope(scope);", //
        "    TestAClassThatNeedsInjection testAClassThatNeedsInjection = new TestAClassThatNeedsInjection();", //
        "    memberInjector.inject(testAClassThatNeedsInjection, scope);", //
        "    return testAClassThatNeedsInjection;", //
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
  public void testAClassThatNeedsInjection_shouldHaveAFactoryThatInjectsIt_whenItHasAnInjectedMethod() {
    JavaFileObject source = JavaFileObjects.forSourceString("test.TestAClassThatNeedsInjection", Joiner.on('\n').join(//
        "package test;", //
        "import javax.inject.Inject;", //
        "public class TestAClassThatNeedsInjection {", //
        "  @Inject public TestAClassThatNeedsInjection() {}", //
        "  @Inject public void m(String s) {}", //
        "}" //
    ));

    JavaFileObject expectedSource = JavaFileObjects.forSourceString("test/TestAClassThatNeedsInjection$$Factory", Joiner.on('\n').join(//
        "package test;", //
        "import java.lang.Override;", //
        "import toothpick.Factory;", //
        "import toothpick.MemberInjector;", //
        "import toothpick.Scope;", //
        "", //
        "public final class TestAClassThatNeedsInjection$$Factory implements Factory<TestAClassThatNeedsInjection> {", //
        "  private MemberInjector<TestAClassThatNeedsInjection> memberInjector = new test.TestAClassThatNeedsInjection$$MemberInjector();", //

        "  @Override", //
        "  public TestAClassThatNeedsInjection createInstance(Scope scope) {", //
        "    scope = getTargetScope(scope);", //
        "    TestAClassThatNeedsInjection testAClassThatNeedsInjection = new TestAClassThatNeedsInjection();", //
        "    memberInjector.inject(testAClassThatNeedsInjection, scope);", //
        "    return testAClassThatNeedsInjection;", //
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
  public void testNonEmptyConstructor() {
    JavaFileObject source = JavaFileObjects.forSourceString("test.TestNonEmptyConstructor", Joiner.on('\n').join(//
        "package test;", //
        "import javax.inject.Inject;", //
        "public class TestNonEmptyConstructor {", //
        "  @Inject public TestNonEmptyConstructor(String str, Integer n) {}", //
        "}" //
    ));

    JavaFileObject expectedSource = JavaFileObjects.forSourceString("test/TestNonEmptyConstructor$$Factory", Joiner.on('\n').join(//
        "package test;", //
        "import java.lang.Integer;", //
        "import java.lang.Override;", //
        "import java.lang.String;", //
        "import toothpick.Factory;", //
        "import toothpick.Scope;", //
        "", //
        "public final class TestNonEmptyConstructor$$Factory implements Factory<TestNonEmptyConstructor> {", //
        "  @Override", //
        "  public TestNonEmptyConstructor createInstance(Scope scope) {", //
        "    scope = getTargetScope(scope);", //
        "    String param1 = scope.getInstance(String.class);", //
        "    Integer param2 = scope.getInstance(Integer.class);", //
        "    TestNonEmptyConstructor testNonEmptyConstructor = new TestNonEmptyConstructor(param1, param2);", //
        "    return testNonEmptyConstructor;", //
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
        .processedWith(ProcessorTestUtilities.factoryProcessors())
        .compilesWithoutError()
        .and()
        .generatesSources(expectedSource);
  }

  @Test
  public void testAbstractClassWithInjectedConstructor() {
    JavaFileObject source = JavaFileObjects.forSourceString("test.TestInvalidClassConstructor", Joiner.on('\n').join(//
        "package test;", //
        "import javax.inject.Inject;", //
        "public abstract class TestInvalidClassConstructor {", //
        "  @Inject public TestInvalidClassConstructor() {}", //
        "}" //
    ));

    assert_().about(javaSource())
        .that(source)
        .processedWith(ProcessorTestUtilities.factoryProcessors())
        .failsToCompile()
        .withErrorContaining("The class test.TestInvalidClassConstructor is abstract or private. It cannot have an injected constructor.");
  }

  @Test
  public void testAClassWithSingletonAnnotation_shouldHaveAFactoryThatSaysItIsASingleton() {
    JavaFileObject source = JavaFileObjects.forSourceString("test.TestNonEmptyConstructor", Joiner.on('\n').join(//
        "package test;", //
        "import javax.inject.Inject;", //
        "import javax.inject.Singleton;", //
        "@Singleton", //
        "public final class TestNonEmptyConstructor {", //
        "  @Inject public TestNonEmptyConstructor(String str, Integer n) {}", //
        "}" //
    ));

    JavaFileObject expectedSource = JavaFileObjects.forSourceString("test/TestEmptyNonConstructor$$Factory", Joiner.on('\n').join(//
        "package test;", //
        "import java.lang.Integer;", //
        "import java.lang.Override;", //
        "import java.lang.String;", //
        "import toothpick.Factory;", //
        "import toothpick.Scope;", //
        "", //
        "public final class TestNonEmptyConstructor$$Factory implements Factory<TestNonEmptyConstructor> {", //
        "  @Override", //
        "  public TestNonEmptyConstructor createInstance(Scope scope) {", //
        "    scope = getTargetScope(scope);", //
        "    String param1 = scope.getInstance(String.class);", //
        "    Integer param2 = scope.getInstance(Integer.class);", //
        "    TestNonEmptyConstructor testNonEmptyConstructor = new TestNonEmptyConstructor(param1, param2);", //
        "    return testNonEmptyConstructor;", //
        "  }", //
        "  @Override", //
        "  public Scope getTargetScope(Scope scope) {", //
        "    return scope.getRootScope();", //
        "  }", //
        "  @Override", //
        "  public boolean hasScopeAnnotation() {", //
        "    return true;", //
        "  }", //
        "  @Override", //
        "  public boolean hasScopeInstancesAnnotation() {", //
        "    return false;", //
        "  }", //
        "}" //
    ));

    assert_().about(javaSource())
        .that(source)
        .processedWith(ProcessorTestUtilities.factoryProcessors())
        .compilesWithoutError()
        .and()
        .generatesSources(expectedSource);
  }

  @Test
  public void testAClassWithEmptyScopedAnnotation_shouldHaveAFactoryThatSaysItIsScopedInCurrentScope() {
    JavaFileObject source = JavaFileObjects.forSourceString("test.TestNonEmptyConstructor", Joiner.on('\n').join(//
        "package test;", //
        "import javax.inject.Inject;", //
        "import javax.inject.Scope;", //
        "@Scope", //
        "@interface CustomScope {}", //
        "@CustomScope", //
        "public final class TestNonEmptyConstructor {", //
        "  @Inject public TestNonEmptyConstructor(String str, Integer n) {}", //
        "  public @interface FooScope {}", //
        "}" //
    ));

    JavaFileObject expectedSource = JavaFileObjects.forSourceString("test/TestEmptyNonConstructor$$Factory", Joiner.on('\n').join(//
        "package test;", //
        "import java.lang.Integer;", //
        "import java.lang.Override;", //
        "import java.lang.String;", //
        "import toothpick.Factory;", //
        "import toothpick.Scope;", //
        "", //
        "public final class TestNonEmptyConstructor$$Factory implements Factory<TestNonEmptyConstructor> {", //
        "  @Override", //
        "  public TestNonEmptyConstructor createInstance(Scope scope) {", //
        "    scope = getTargetScope(scope);", //
        "    String param1 = scope.getInstance(String.class);", //
        "    Integer param2 = scope.getInstance(Integer.class);", //
        "    TestNonEmptyConstructor testNonEmptyConstructor = new TestNonEmptyConstructor(param1, param2);", //
        "    return testNonEmptyConstructor;", //
        "  }", //
        "  @Override", //
        "  public Scope getTargetScope(Scope scope) {", //
        "    return scope.getParentScope(test.CustomScope.class);", //
        "  }", //
        "  @Override", //
        "  public boolean hasScopeAnnotation() {", //
        "    return true;", //
        "  }", //
        "  @Override", //
        "  public boolean hasScopeInstancesAnnotation() {", //
        "    return false;", //
        "  }", //
        "}" //
    ));

    assert_().about(javaSource())
        .that(source)
        .processedWith(ProcessorTestUtilities.factoryProcessors())
        .compilesWithoutError()
        .and()
        .generatesSources(expectedSource);
  }

  @Test
  public void testAClassWithProvidesSingletonAnnotation_shouldHaveAFactoryThatSaysItIsAProvidesSingleton() {
    JavaFileObject source = JavaFileObjects.forSourceString("test.TestNonEmptyConstructor", Joiner.on('\n').join(//
        "package test;", //
        "import javax.inject.Inject;", //
        "import javax.inject.Singleton;", //
        "import toothpick.ScopeInstances;", //
        "@ScopeInstances @Singleton", //
        "public class TestNonEmptyConstructor {", //
        "  @Inject public TestNonEmptyConstructor(String str, Integer n) {}", //
        "}" //
    ));

    JavaFileObject expectedSource = JavaFileObjects.forSourceString("test/TestEmptyNonConstructor$$Factory", Joiner.on('\n').join(//
        "package test;", //
        "import java.lang.Integer;", //
        "import java.lang.Override;", //
        "import java.lang.String;", //
        "import toothpick.Factory;", //
        "import toothpick.Scope;", //
        "", //
        "public final class TestNonEmptyConstructor$$Factory implements Factory<TestNonEmptyConstructor> {", //
        "  @Override", //
        "  public TestNonEmptyConstructor createInstance(Scope scope) {", //
        "    scope = getTargetScope(scope);", //
        "    String param1 = scope.getInstance(String.class);", //
        "    Integer param2 = scope.getInstance(Integer.class);", //
        "    TestNonEmptyConstructor testNonEmptyConstructor = new TestNonEmptyConstructor(param1, param2);", //
        "    return testNonEmptyConstructor;", //
        "  }", //
        "  @Override", //
        "  public Scope getTargetScope(Scope scope) {", //
        "    return scope.getRootScope();", //
        "  }", //
        "  @Override", //
        "  public boolean hasScopeAnnotation() {", //
        "    return true;", //
        "  }", //
        "  @Override", //
        "  public boolean hasScopeInstancesAnnotation() {", //
        "    return true;", //
        "  }", //
        "}" //
    ));

    assert_().about(javaSource())
        .that(source)
        .processedWith(ProcessorTestUtilities.factoryProcessors())
        .compilesWithoutError()
        .and()
        .generatesSources(expectedSource);
  }
}
