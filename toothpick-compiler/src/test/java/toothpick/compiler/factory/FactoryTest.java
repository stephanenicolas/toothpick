package toothpick.compiler.factory;

import com.google.common.base.Joiner;
import com.google.testing.compile.JavaFileObjects;
import javax.tools.JavaFileObject;
import javax.tools.StandardLocation;
import org.junit.Test;

import static com.google.common.truth.Truth.assert_;
import static com.google.testing.compile.JavaSourceSubjectFactory.javaSource;
import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

public class FactoryTest {
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
        "    TestEmptyConstructor testEmptyConstructor = new TestEmptyConstructor();", //
        "    return testEmptyConstructor;", //
        "  }", //
        "  @Override", //
        "  public boolean hasSingletonAnnotation() {", //
        "    return false;", //
        "  }", //
        "  @Override", //
        "  public boolean hasProducesSingletonAnnotation() {", //
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
        "    Test2Constructors test2Constructors = new Test2Constructors();", //
        "    return test2Constructors;", //
        "  }", //
        "  @Override", //
        "  public boolean hasSingletonAnnotation() {", //
        "    return false;", //
        "  }", //
        "  @Override", //
        "  public boolean hasProducesSingletonAnnotation() {", //
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
  public void testAClassThatNeedsInjection_withAnInjectedField() {
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
        "import toothpick.Scope;", //
        "", //
        "public final class TestAClassThatNeedsInjection$$Factory implements Factory<TestAClassThatNeedsInjection> {", //
        "  @Override", //
        "  public TestAClassThatNeedsInjection createInstance(Scope scope) {", //
        "    TestAClassThatNeedsInjection testAClassThatNeedsInjection = new TestAClassThatNeedsInjection();", //
        "    new test.TestAClassThatNeedsInjection$$MemberInjector().inject(testAClassThatNeedsInjection, scope);", //
        "    return testAClassThatNeedsInjection;", //
        "  }", //
        "  @Override", //
        "  public boolean hasSingletonAnnotation() {", //
        "    return false;", //
        "  }", //
        "  @Override", //
        "  public boolean hasProducesSingletonAnnotation() {", //
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
  public void testAClassThatNeedsInjection_withAnInjectedMethod() {
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
        "import toothpick.Scope;", //
        "", //
        "public final class TestAClassThatNeedsInjection$$Factory implements Factory<TestAClassThatNeedsInjection> {", //
        "  @Override", //
        "  public TestAClassThatNeedsInjection createInstance(Scope scope) {", //
        "    TestAClassThatNeedsInjection testAClassThatNeedsInjection = new TestAClassThatNeedsInjection();", //
        "    new test.TestAClassThatNeedsInjection$$MemberInjector().inject(testAClassThatNeedsInjection, scope);", //
        "    return testAClassThatNeedsInjection;", //
        "  }", //
        "  @Override", //
        "  public boolean hasSingletonAnnotation() {", //
        "    return false;", //
        "  }", //
        "  @Override", //
        "  public boolean hasProducesSingletonAnnotation() {", //
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
        "    String param1 = scope.getInstance(String.class);", //
        "    Integer param2 = scope.getInstance(Integer.class);", //
        "    TestNonEmptyConstructor testNonEmptyConstructor = new TestNonEmptyConstructor(param1, param2);", //
        "    return testNonEmptyConstructor;", //
        "  }", //
        "  @Override", //
        "  public boolean hasSingletonAnnotation() {", //
        "    return false;", //
        "  }", //
        "  @Override", //
        "  public boolean hasProducesSingletonAnnotation() {", //
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
  public void testInvalidClassConstructor() {
    JavaFileObject source = JavaFileObjects.forSourceString("test.TestInvalidClassConstructor", Joiner.on('\n').join(//
        "package test;", //
        "import javax.inject.Inject;", //
        "public abstract class TestInvalidClassConstructor {", //
        "  @Inject public TestInvalidClassConstructor() {}", //
        "}" //
    ));

    assertThatCompileWithoutErrorButNoFactoryIsNotCreated(source, "test", "TestAbstractClassConstructor");
  }

  @Test
  public void testSingletonAnnotation() {
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
        "    String param1 = scope.getInstance(String.class);", //
        "    Integer param2 = scope.getInstance(Integer.class);", //
        "    TestNonEmptyConstructor testNonEmptyConstructor = new TestNonEmptyConstructor(param1, param2);", //
        "    return testNonEmptyConstructor;", //
        "  }", //
        "  @Override", //
        "  public boolean hasSingletonAnnotation() {", //
        "    return true;", //
        "  }", //
        "  @Override", //
        "  public boolean hasProducesSingletonAnnotation() {", //
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
  public void testProducesSingletonAnnotation() {
    JavaFileObject source = JavaFileObjects.forSourceString("test.TestNonEmptyConstructor", Joiner.on('\n').join(//
        "package test;", //
        "import javax.inject.Inject;", //
        "import toothpick.ProvidesSingleton;", //
        "@ProvidesSingleton", //
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
        "    String param1 = scope.getInstance(String.class);", //
        "    Integer param2 = scope.getInstance(Integer.class);", //
        "    TestNonEmptyConstructor testNonEmptyConstructor = new TestNonEmptyConstructor(param1, param2);", //
        "    return testNonEmptyConstructor;", //
        "  }", //
        "  @Override", //
        "  public boolean hasSingletonAnnotation() {", //
        "    return false;", //
        "  }", //
        "  @Override", //
        "  public boolean hasProducesSingletonAnnotation() {", //
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
  public void testOptimisticFactoryCreationForInjectedField() {
    JavaFileObject source = JavaFileObjects.forSourceString("test.TestOptimisticFactoryCreationForInjectedField", Joiner.on('\n').join(//
        "package test;", //
        "import javax.inject.Inject;", //
        "import toothpick.ProvidesSingleton;", //
        "@ProvidesSingleton", //
        "public class TestOptimisticFactoryCreationForInjectedField {", //
        "  @Inject Foo foo;", //
        "}", //
        "  class Foo {}"));

    assert_().about(javaSource())
        .that(source)
        .processedWith(ProcessorTestUtilities.factoryProcessors())
        .compilesWithoutError()
        .and()
        .generatesFileNamed(StandardLocation.locationFor("CLASS_OUTPUT"), "test", "Foo$$Factory.class");
  }

  @Test
  public void testOptimisticFactoryCreationForInjectedField_shouldFail_WhenFieldIsInvalid() {
    JavaFileObject source = JavaFileObjects.forSourceString("test.TestOptimisticFactoryCreationForInjectedField", Joiner.on('\n').join(//
        "package test;", //
        "import javax.inject.Inject;", //
        "import toothpick.ProvidesSingleton;", //
        "@ProvidesSingleton", //
        "public class TestOptimisticFactoryCreationForInjectedField {", //
        "  @Inject private Foo foo;", //
        "}", //
        "  class Foo {}"));

    assert_().about(javaSource())
        .that(source)
        .processedWith(ProcessorTestUtilities.factoryProcessors())
        .failsToCompile()
        .withErrorContaining("@Inject annotated fields must be non private : test.TestOptimisticFactoryCreationForInjectedField.foo");
  }

  @Test
  public void testOptimisticFactoryCreationForInjectedField_shouldWorkButNoFactoryIsProduced_whenTypeIsAbstract() {
    JavaFileObject source = JavaFileObjects.forSourceString("test.TestOptimisticFactoryCreationForInjectedField", Joiner.on('\n').join(//
        "package test;", //
        "import javax.inject.Inject;", //
        "import toothpick.ProvidesSingleton;", //
        "@ProvidesSingleton", //
        "public class TestOptimisticFactoryCreationForInjectedField {", //
        "  @Inject Foo foo;", //
        "}", //
        "  abstract class Foo {}"));

    assertThatCompileWithoutErrorButNoFactoryIsNotCreated(source, "test", "Foo");
  }

  @Test
  public void testOptimisticFactoryCreationForInjectedField_shouldWorkButNoFactoryIsProduced_whenTypeHasANonDefaultConstructor() {
    JavaFileObject source = JavaFileObjects.forSourceString("test.TestOptimisticFactoryCreationForInjectedField", Joiner.on('\n').join(//
        "package test;", //
        "import javax.inject.Inject;", //
        "import toothpick.ProvidesSingleton;", //
        "@ProvidesSingleton", //
        "public class TestOptimisticFactoryCreationForInjectedField {", //
        "  @Inject Foo foo;", //
        "}", //
        "class Foo {", //
        " public Foo(String s) {}", //
        "}"));

    assertThatCompileWithoutErrorButNoFactoryIsNotCreated(source, "test", "Foo");
  }

  @Test
  public void testOptimisticFactoryCreationForInjectedField_shouldWorkButNoFactoryIsProduced_whenTypeHasAPrivateDefaultConstructor() {
    JavaFileObject source = JavaFileObjects.forSourceString("test.TestOptimisticFactoryCreationForInjectedField", Joiner.on('\n').join(//
        "package test;", //
        "import javax.inject.Inject;", //
        "import toothpick.ProvidesSingleton;", //
        "@ProvidesSingleton", //
        "public class TestOptimisticFactoryCreationForInjectedField {", //
        "  @Inject Foo foo;", //
        "}", //
        "class Foo {", //
        " private Foo() {}", //
        "}"));

    assertThatCompileWithoutErrorButNoFactoryIsNotCreated(source, "test", "Foo");
  }

  @Test
  public void testOptimisticFactoryCreationForInjectedMethod() {
    JavaFileObject source = JavaFileObjects.forSourceString("test.TestOptimisticFactoryCreationForInjectedMethod", Joiner.on('\n').join(//
        "package test;", //
        "import javax.inject.Inject;", //
        "import toothpick.ProvidesSingleton;", //
        "@ProvidesSingleton", //
        "public class TestOptimisticFactoryCreationForInjectedMethod {", //
        "  @Inject void m(Foo foo) {}", //
        "}", //
        "  class Foo {}"));

    assert_().about(javaSource())
        .that(source)
        .processedWith(ProcessorTestUtilities.factoryProcessors())
        .compilesWithoutError()
        .and()
        .generatesFileNamed(StandardLocation.locationFor("CLASS_OUTPUT"), "test", "Foo$$Factory.class");
  }

  @Test
  public void testOptimisticFactoryCreationForInjectedMethod_shouldWorkButNoFactoryIsProduced_whenTypeIsPrivate() {
    JavaFileObject source = JavaFileObjects.forSourceString("test.TestOptimisticFactoryCreationForInjectedMethod", Joiner.on('\n').join(//
        "package test;", //
        "import javax.inject.Inject;", //
        "import toothpick.ProvidesSingleton;", //
        "@ProvidesSingleton", //
        "public class TestOptimisticFactoryCreationForInjectedMethod {", //
        "  @Inject void m(Foo foo) {}", //
        "  private static class Foo {}", //
        "}"));

    assertThatCompileWithoutErrorButNoFactoryIsNotCreated(source, "test", "Foo");
  }

  @Test
  public void testOptimisticFactoryCreationForInjectedMethod_shouldFail_whenMethodIsInvalid() {
    JavaFileObject source = JavaFileObjects.forSourceString("test.TestOptimisticFactoryCreationForInjectedMethod", Joiner.on('\n').join(//
        "package test;", //
        "import javax.inject.Inject;", //
        "import toothpick.ProvidesSingleton;", //
        "@ProvidesSingleton", //
        "public class TestOptimisticFactoryCreationForInjectedMethod {", //
        "  @Inject private void m(Foo foo) {}", //
        "  private static class Foo {}", //
        "}"));

    assert_().about(javaSource())
        .that(source)
        .processedWith(ProcessorTestUtilities.factoryProcessors())
        .failsToCompile()
        .withErrorContaining("@Inject annotated methods must not be private : test.TestOptimisticFactoryCreationForInjectedMethod.m");
  }

  @Test
  public void testOptimisticFactoryCreationForInjectedConstructor() {
    JavaFileObject source = JavaFileObjects.forSourceString("test.TestOptimisticFactoryCreationForInjectedConstructor", Joiner.on('\n').join(//
        "package test;", //
        "import javax.inject.Inject;", //
        "import toothpick.ProvidesSingleton;", //
        "@ProvidesSingleton", //
        "public class TestOptimisticFactoryCreationForInjectedConstructor {", //
        "  @Inject void TestOptimisticFactoryCreationForInjectedConstructor(Foo foo) {}", //
        "}", //
        "  class Foo {}"));

    assert_().about(javaSource())
        .that(source)
        .processedWith(ProcessorTestUtilities.factoryProcessors())
        .compilesWithoutError()
        .and()
        .generatesFileNamed(StandardLocation.locationFor("CLASS_OUTPUT"), "test", "Foo$$Factory.class");
  }

  @Test
  public void testOptimisticFactoryCreationForInjectedConstructor_shouldWorkButNoFactoryIsProduced_whenTypeIsInterface() {
    JavaFileObject source = JavaFileObjects.forSourceString("test.TestOptimisticFactoryCreationForInjectedConstructor", Joiner.on('\n').join(//
        "package test;", //
        "import javax.inject.Inject;", //
        "import toothpick.ProvidesSingleton;", //
        "@ProvidesSingleton", //
        "public class TestOptimisticFactoryCreationForInjectedConstructor {", //
        "  @Inject void TestOptimisticFactoryCreationForInjectedConstructor(Foo foo) {}", //
        "}", //
        "  interface Foo {}"));

    assertThatCompileWithoutErrorButNoFactoryIsNotCreated(source, "test", "Foo");
  }

  private void assertThatCompileWithoutErrorButNoFactoryIsNotCreated(JavaFileObject source, String noFactoryPackageName, String noFactoryClass) {
    try {
      assert_().about(javaSource())
          .that(source)
          .processedWith(ProcessorTestUtilities.factoryProcessors())
          .compilesWithoutError()
          .and()
          .generatesFileNamed(StandardLocation.locationFor("CLASS_OUTPUT"), "test", noFactoryClass + "$$Factory.class");
      fail("No optimistic factory should be created for an interface");
    } catch (AssertionError e) {
      assertThat(e.getMessage(), containsString(
          String.format("Did not find a generated file corresponding to %s$$Factory.class in package %s;", noFactoryClass, noFactoryPackageName)));
    }
  }
}
