package toothpick.compiler.factory;

import com.google.common.base.Joiner;
import com.google.testing.compile.JavaFileObjects;
import javax.tools.JavaFileObject;
import org.junit.Test;

import static com.google.common.truth.Truth.assert_;
import static com.google.testing.compile.JavaSourceSubjectFactory.javaSource;

public class FactoryTest extends BaseFactoryTest {
  @Test
  public void testEmptyConstructor_shouldWork_whenConstructorIsPublic() {
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
        "  public boolean hasProvidesSingletonInScopeAnnotation() {", //
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
  public void testEmptyConstructor_shouldWork_whenConstructorIsPackage() {
    JavaFileObject source = JavaFileObjects.forSourceString("test.TestEmptyConstructor", Joiner.on('\n').join(//
        "package test;", //
        "import javax.inject.Inject;", //
        "public class TestEmptyConstructor {", //
        "  @Inject TestEmptyConstructor() {}", //
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
        "  public boolean hasProvidesSingletonInScopeAnnotation() {", //
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
  public void testEmptyConstructor_shouldWork_whenConstructorIsProtected() {
    JavaFileObject source = JavaFileObjects.forSourceString("test.TestEmptyConstructor", Joiner.on('\n').join(//
        "package test;", //
        "import javax.inject.Inject;", //
        "public class TestEmptyConstructor {", //
        "  @Inject protected TestEmptyConstructor() {}", //
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
        "  public boolean hasProvidesSingletonInScopeAnnotation() {", //
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
  public void testInjectedConstructorInPrivateClass_shouldNotAllowInjectionInPrivateClasses() {
    JavaFileObject source = JavaFileObjects.forSourceString("test.TestConstructorInPrivateClass", Joiner.on('\n').join(//
        "package test;", //
        "import javax.inject.Inject;", //
        "class Wrapper {", //
        "  private class TestConstructorInPrivateClass {", //
        "    @Inject public TestConstructorInPrivateClass() {}", //
        "  }", //
        "}" //
    ));

    assert_().about(javaSource())
        .that(source)
        .processedWith(ProcessorTestUtilities.factoryProcessors())
        .failsToCompile()
        .withErrorContaining("Class test.Wrapper.TestConstructorInPrivateClass is private. @Inject constructors are not allowed in private classes.");
  }

  @Test
  public void testInjectedConstructorInProtectedClass_shouldWork() {
    JavaFileObject source = JavaFileObjects.forSourceString("test.TestConstructorInProtectedClass", Joiner.on('\n').join(//
        "package test;", //
        "import javax.inject.Inject;", //
        "class Wrapper {", //
        "  protected static class TestConstructorInProtectedClass {", //
        "    @Inject public TestConstructorInProtectedClass() {}", //
        "  }", //
        "}" //
    ));

    JavaFileObject expectedSource = JavaFileObjects.forSourceString("test/Wrapper$TestConstructorInProtectedClass$$Factory", Joiner.on('\n').join(//
        "package test;", //
        "import java.lang.Override;", //
        "import toothpick.Factory;", //
        "import toothpick.Scope;", //
        "", //
        "public final class Wrapper$TestConstructorInProtectedClass$$Factory implements Factory<Wrapper.TestConstructorInProtectedClass> {", //
        "  @Override", //
        "  public Wrapper.TestConstructorInProtectedClass createInstance(Scope scope) {", //
        "    Wrapper.TestConstructorInProtectedClass testConstructorInProtectedClass = new Wrapper.TestConstructorInProtectedClass();", //
        "    return testConstructorInProtectedClass;", //
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
        "  public boolean hasProvidesSingletonInScopeAnnotation() {", //
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
  public void testInjectedConstructorInPackageClass_shouldWork() {
    JavaFileObject source = JavaFileObjects.forSourceString("test.TestConstructorInPackageClass", Joiner.on('\n').join(//
        "package test;", //
        "import javax.inject.Inject;", //
        "class TestConstructorInPackageClass {", //
        "  @Inject public TestConstructorInPackageClass() {}", //
        "}" //
    ));

    JavaFileObject expectedSource = JavaFileObjects.forSourceString("test/TestConstructorInPackageClass$$Factory", Joiner.on('\n').join(//
        "package test;", //
        "import java.lang.Override;", //
        "import toothpick.Factory;", //
        "import toothpick.Scope;", //
        "", //
        "public final class TestConstructorInPackageClass$$Factory implements Factory<TestConstructorInPackageClass> {", //
        "  @Override", //
        "  public TestConstructorInPackageClass createInstance(Scope scope) {", //
        "    TestConstructorInPackageClass testConstructorInPackageClass = new TestConstructorInPackageClass();", //
        "    return testConstructorInPackageClass;", //
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
        "  public boolean hasProvidesSingletonInScopeAnnotation() {", //
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
        "  public boolean hasProvidesSingletonInScopeAnnotation() {", //
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
        "  public boolean hasProvidesSingletonInScopeAnnotation() {", //
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
  public void testAInnerClassThatNeedsInjection_shouldHaveAFactoryThatInjectsIt_whenItHasAnInjectedField() {
    JavaFileObject source = JavaFileObjects.forSourceString("test.TestAInnerClassThatNeedsInjection", Joiner.on('\n').join(//
        "package test;", //
        "import javax.inject.Inject;", //
        "public class TestAInnerClassThatNeedsInjection {", //
        "  public static class InnerClass  {", //
        "    @Inject String s;", //
        "    public InnerClass() {}", //
        "  }", //
        "}" //
    ));

    JavaFileObject expectedSource = JavaFileObjects.forSourceString("test/TestAInnerClassThatNeedsInjection$InnerClass$$Factory", Joiner.on('\n').join(//
        "package test;", //
        "import java.lang.Override;", //
        "import toothpick.Factory;", //
        "import toothpick.MemberInjector;", //
        "import toothpick.Scope;", //
        "", //
        "public final class TestAInnerClassThatNeedsInjection$InnerClass$$Factory implements Factory<TestAInnerClassThatNeedsInjection.InnerClass> {", //
        "  private MemberInjector<TestAInnerClassThatNeedsInjection.InnerClass> memberInjector = new test.TestAInnerClassThatNeedsInjection$InnerClass$$MemberInjector();", //
        "  @Override", //
        "  public TestAInnerClassThatNeedsInjection.InnerClass createInstance(Scope scope) {", //
        "    scope = getTargetScope(scope);", //
        "    TestAInnerClassThatNeedsInjection.InnerClass innerClass = new TestAInnerClassThatNeedsInjection.InnerClass();", //
        "    memberInjector.inject(innerClass, scope);", //
        "    return innerClass;", //
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
        "  public boolean hasProvidesSingletonInScopeAnnotation() {", //
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
  public void testAClassThatInheritFromAnotherClassThatNeedsInjection_shouldHaveAFactoryThatInjectsIt_whenItHasAnAnnotatedConstructor_andShouldUseSuperMemberInjector() {
    JavaFileObject source = JavaFileObjects.forSourceString("test.TestAClassThatNeedsInjection", Joiner.on('\n').join(//
        "package test;", //
        "import javax.inject.Inject;", //
        "public class TestAClassThatNeedsInjection extends SuperClassThatNeedsInjection {", //
        "  @Inject public TestAClassThatNeedsInjection() {}", //
        "}", //
        "class SuperClassThatNeedsInjection {", //
        "  @Inject String s;", //
        "  public SuperClassThatNeedsInjection() {}", //
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
        "  private MemberInjector<SuperClassThatNeedsInjection> memberInjector = new test.SuperClassThatNeedsInjection$$MemberInjector();", //
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
        "  public boolean hasProvidesSingletonInScopeAnnotation() {", //
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
        "  public boolean hasProvidesSingletonInScopeAnnotation() {", //
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
        "  public boolean hasProvidesSingletonInScopeAnnotation() {", //
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
  public void testNonEmptyConstructorWithLazy() {
    JavaFileObject source = JavaFileObjects.forSourceString("test.TestNonEmptyConstructor", Joiner.on('\n').join(//
        "package test;", //
        "import javax.inject.Inject;", //
        "import toothpick.Lazy;", //
        "public class TestNonEmptyConstructor {", //
        "  @Inject public TestNonEmptyConstructor(Lazy<String> str, Integer n) {}", //
        "}" //
    ));

    JavaFileObject expectedSource = JavaFileObjects.forSourceString("test/TestNonEmptyConstructor$$Factory", Joiner.on('\n').join(//
        "package test;", //
        "import java.lang.Integer;", //
        "import java.lang.Override;", //
        "import java.lang.String;", //
        "import toothpick.Factory;", //
        "import toothpick.Lazy;", //
        "import toothpick.Scope;", //
        "", //
        "public final class TestNonEmptyConstructor$$Factory implements Factory<TestNonEmptyConstructor> {", //
        "  @Override", //
        "  public TestNonEmptyConstructor createInstance(Scope scope) {", //
        "    scope = getTargetScope(scope);", //
        "    Lazy<String> param1 = scope.getLazy(String.class);", //
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
        "  public boolean hasProvidesSingletonInScopeAnnotation() {", //
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
  public void testNonEmptyConstructorWithProvider() {
    JavaFileObject source = JavaFileObjects.forSourceString("test.TestNonEmptyConstructor", Joiner.on('\n').join(//
        "package test;", //
        "import javax.inject.Inject;", //
        "import javax.inject.Provider;", //
        "public class TestNonEmptyConstructor {", //
        "  @Inject public TestNonEmptyConstructor(Provider<String> str, Integer n) {}", //
        "}" //
    ));

    JavaFileObject expectedSource = JavaFileObjects.forSourceString("test/TestNonEmptyConstructor$$Factory", Joiner.on('\n').join(//
        "package test;", //
        "import java.lang.Integer;", //
        "import java.lang.Override;", //
        "import java.lang.String;", //
        "import javax.inject.Provider;", //
        "import toothpick.Factory;", //
        "import toothpick.Scope;", //
        "", //
        "public final class TestNonEmptyConstructor$$Factory implements Factory<TestNonEmptyConstructor> {", //
        "  @Override", //
        "  public TestNonEmptyConstructor createInstance(Scope scope) {", //
        "    scope = getTargetScope(scope);", //
        "    Provider<String> param1 = scope.getProvider(String.class);", //
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
        "  public boolean hasProvidesSingletonInScopeAnnotation() {", //
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
  public void testNonEmptyConstructor_shouldFail_whenContainsInvalidLazyParameter() {
    JavaFileObject source = JavaFileObjects.forSourceString("test.TestNonEmptyConstructor", Joiner.on('\n').join(//
        "package test;", //
        "import javax.inject.Inject;", //
        "import toothpick.Lazy;", //
        "public class TestNonEmptyConstructor {", //
        "  @Inject public TestNonEmptyConstructor(Lazy lazy, Integer n) {}", //
        "}" //
    ));

    assert_().about(javaSource())
        .that(source)
        .processedWith(ProcessorTestUtilities.factoryProcessors())
        .failsToCompile()
        .withErrorContaining("Parameter lazy in method/constructor test.TestNonEmptyConstructor#<init> is not a valid toothpick.Lazy.");
  }

  @Test
  public void testNonEmptyConstructor_shouldFail_whenContainsInvalidProviderParameter() {
    JavaFileObject source = JavaFileObjects.forSourceString("test.TestNonEmptyConstructor", Joiner.on('\n').join(//
        "package test;", //
        "import javax.inject.Inject;", //
        "import javax.inject.Provider;", //
        "public class TestNonEmptyConstructor {", //
        "  @Inject public TestNonEmptyConstructor(Provider provider, Integer n) {}", //
        "}" //
    ));

    assert_().about(javaSource())
        .that(source)
        .processedWith(ProcessorTestUtilities.factoryProcessors())
        .failsToCompile()
        .withErrorContaining("Parameter provider in method/constructor test.TestNonEmptyConstructor#<init> is not a valid javax.inject.Provider.");
  }

  @Test
  public void testNonEmptyConstructorWithGenerics() {
    JavaFileObject source = JavaFileObjects.forSourceString("test.TestNonEmptyConstructor", Joiner.on('\n').join(//
        "package test;", //
        "import java.util.List;", //
        "import javax.inject.Inject;", //
        "public class TestNonEmptyConstructor {", //
        "  @Inject public TestNonEmptyConstructor(List<String> str) {}", //
        "}" //
    ));

    JavaFileObject expectedSource = JavaFileObjects.forSourceString("test/TestNonEmptyConstructor$$Factory", Joiner.on('\n').join(//
        "package test;", //
        "import java.lang.Override;", //
        "import java.util.List;", //
        "import toothpick.Factory;", //
        "import toothpick.Scope;", //
        "", //
        "public final class TestNonEmptyConstructor$$Factory implements Factory<TestNonEmptyConstructor> {", //
        "  @Override", //
        "  public TestNonEmptyConstructor createInstance(Scope scope) {", //
        "    scope = getTargetScope(scope);", //
        "    List param1 = scope.getInstance(List.class);", //
        "    TestNonEmptyConstructor testNonEmptyConstructor = new TestNonEmptyConstructor(param1);", //
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
        "  public boolean hasProvidesSingletonInScopeAnnotation() {", //
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
  public void testNonEmptyConstructorWithLazyAndGenerics_shouldFail() {
    JavaFileObject source = JavaFileObjects.forSourceString("test.TestNonEmptyConstructor", Joiner.on('\n').join(//
        "package test;", //
        "import java.util.List;", //
        "import javax.inject.Inject;", //
        "import toothpick.Lazy;", //
        "public class TestNonEmptyConstructor {", //
        "  @Inject public TestNonEmptyConstructor(Lazy<List<String>> str) {}", //
        "}" //
    ));

    assert_().about(javaSource())
        .that(source)
        .processedWith(ProcessorTestUtilities.factoryProcessors())
        .failsToCompile()
        .withErrorContaining("Lazy/Provider str is not a valid in <init>. Lazy/Provider cannot be used on generic types.");
  }

  @Test
  public void testNonEmptyConstructorWithProviderAndGenerics_shouldFail() {
    JavaFileObject source = JavaFileObjects.forSourceString("test.TestNonEmptyConstructor", Joiner.on('\n').join(//
        "package test;", //
        "import java.util.List;", //
        "import javax.inject.Inject;", //
        "import javax.inject.Provider;", //
        "public class TestNonEmptyConstructor {", //
        "  @Inject public TestNonEmptyConstructor(Provider<List<String>> str) {}", //
        "}" //
    ));

    assert_().about(javaSource())
        .that(source)
        .processedWith(ProcessorTestUtilities.factoryProcessors())
        .failsToCompile()
        .withErrorContaining("Lazy/Provider str is not a valid in <init>. Lazy/Provider cannot be used on generic types.");
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
  public void testClassWithInjectedConstructorThrowingException() {
    JavaFileObject source = JavaFileObjects.forSourceString("test.TestClassConstructorThrowingException", Joiner.on('\n').join(//
        "package test;", //
        "import javax.inject.Inject;", //
        "public class TestClassConstructorThrowingException {", //
        "  @Inject public TestClassConstructorThrowingException(String s) throws Exception {}", //
        "}" //
    ));

    JavaFileObject expectedSource = JavaFileObjects.forSourceString("test/TestClassConstructorThrowingException$$Factory", Joiner.on('\n').join(//
        "package test;", //
        "import java.lang.Override;", //
        "import java.lang.String;", //
        "import toothpick.Factory;", //
        "import toothpick.Scope;", //
        "", //
        "public final class TestClassConstructorThrowingException$$Factory implements Factory<TestClassConstructorThrowingException> {", //
        "  @Override", //
        "  public TestClassConstructorThrowingException createInstance(Scope scope) {", //
        "    scope = getTargetScope(scope);", //
        "    try {", //
        "      String param1 = scope.getInstance(String.class);", //
        "      TestClassConstructorThrowingException testClassConstructorThrowingException = new TestClassConstructorThrowingException(param1);", //
        "      return testClassConstructorThrowingException;", //
        "    } catch(java.lang.Throwable ex) {", //
        "      throw new java.lang.RuntimeException(ex);", //
        "    }", //
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
        "  public boolean hasProvidesSingletonInScopeAnnotation() {", //
        "    return false;", //
        "  }", //
        "}" //
    ));

    assert_().about(javaSource())
        .that(source)
        .processedWith(ProcessorTestUtilities.factoryProcessors())
        .compilesWithoutError()
        .and()
        .generatesSources(expectedSource);  }

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
        "  public boolean hasProvidesSingletonInScopeAnnotation() {", //
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
  public void testAClassWithSingletonAnnotationAndNoConstructor_shouldHaveAFactoryThatSaysItIsASingleton() {
    JavaFileObject source = JavaFileObjects.forSourceString("test.TestEmptyConstructor", Joiner.on('\n').join(//
        "package test;", //
        "import javax.inject.Inject;", //
        "import javax.inject.Singleton;", //
        "@Singleton", //
        "public final class TestEmptyConstructor {", //
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
        "    TestEmptyConstructor testEmptyConstructor = new TestEmptyConstructor();", //
        "    return testEmptyConstructor;", //
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
        "  public boolean hasProvidesSingletonInScopeAnnotation() {", //
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
        "import java.lang.annotation.Retention;", //
        "import java.lang.annotation.RetentionPolicy;", //
        "@Scope", //
        "@Retention(RetentionPolicy.RUNTIME)", //
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
        "  public boolean hasProvidesSingletonInScopeAnnotation() {", //
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
  public void testAClassWithEmptyScopedAnnotationAndNoConstructor_shouldHaveAFactoryThatSaysItIsScopedInCurrentScope() {
    JavaFileObject source = JavaFileObjects.forSourceString("test.TestEmptyConstructor", Joiner.on('\n').join(//
        "package test;", //
        "import javax.inject.Inject;", //
        "import javax.inject.Scope;", //
        "import java.lang.annotation.Retention;", //
        "import java.lang.annotation.RetentionPolicy;", //
        "@Scope", //
        "@Retention(RetentionPolicy.RUNTIME)", //
        "@interface CustomScope {}", //
        "@CustomScope", //
        "public final class TestEmptyConstructor {", //
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
        "    TestEmptyConstructor testEmptyConstructor = new TestEmptyConstructor();", //
        "    return testEmptyConstructor;", //
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
        "  public boolean hasProvidesSingletonInScopeAnnotation() {", //
        "    return false;", //
        "  }", //
        "}" //
    ));

    assert_().about(javaSource())
        .that(source)
        .processedWith(ProcessorTestUtilities.factoryProcessorsWithAdditionalTypes("test.CustomScope"))
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
        "import toothpick.ProvidesSingletonInScope;", //
        "@ProvidesSingletonInScope @Singleton", //
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
        "  public boolean hasProvidesSingletonInScopeAnnotation() {", //
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

  @Test
  public void testInjectedConstructor_withPrimitiveParam() {
    JavaFileObject source = JavaFileObjects.forSourceString("test.TestPrimitiveConstructor", Joiner.on('\n').join(//
        "package test;", //
        "import javax.inject.Inject;", //
        "public class TestPrimitiveConstructor {", //
        "  @Inject TestPrimitiveConstructor(int n) {}", //
        "}" //
    ));

    assert_().about(javaSource())
        .that(source)
        .processedWith(ProcessorTestUtilities.factoryProcessors())
        .failsToCompile()
        .withErrorContaining("Parameter n in method/constructor test.TestPrimitiveConstructor#<init> is of type int which is not supported by Toothpick.");
  }
}
