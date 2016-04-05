package toothpick.compiler.factory;

import com.google.common.base.Joiner;
import com.google.testing.compile.JavaFileObjects;
import javax.tools.JavaFileObject;
import org.junit.Test;

import static com.google.common.truth.Truth.assert_;
import static com.google.testing.compile.JavaSourceSubjectFactory.javaSource;

public class FactoryTest {
  @Test public void testEmptyConstructor() {
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
        "import toothpick.Injector;", //
        "", //
        "public final class TestEmptyConstructor$$Factory implements Factory<TestEmptyConstructor> {", //
        "  @Override", //
        "  public TestEmptyConstructor createInstance(Injector injector) {", //
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

  @Test public void testPrivateConstructor() {
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

  @Test public void testInjectedConstructorInPrivateClass() {
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

  @Test public void testAClassThatNeedsInjection() {
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
        "import toothpick.Injector;", //
        "", //
        "public final class TestAClassThatNeedsInjection$$Factory implements Factory<TestAClassThatNeedsInjection> {", //
        "  @Override", //
        "  public TestAClassThatNeedsInjection createInstance(Injector injector) {", //
        "    TestAClassThatNeedsInjection testAClassThatNeedsInjection = new TestAClassThatNeedsInjection();", //
        "    injector.inject(testAClassThatNeedsInjection);", //
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
        .processedWith(ProcessorTestUtilities.factoryProcessors())
        .compilesWithoutError()
        .and()
        .generatesSources(expectedSource);
  }

  @Test public void testNonEmptyConstructor() {
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
        "import toothpick.Injector;", //
        "", //
        "public final class TestNonEmptyConstructor$$Factory implements Factory<TestNonEmptyConstructor> {", //
        "  @Override", //
        "  public TestNonEmptyConstructor createInstance(Injector injector) {", //
        "    String param1 = injector.getInstance(String.class);", //
        "    Integer param2 = injector.getInstance(Integer.class);", //
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

  @Test public void testSingletonAnnotation() {
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
        "import toothpick.Injector;", //
        "", //
        "public final class TestNonEmptyConstructor$$Factory implements Factory<TestNonEmptyConstructor> {", //
        "  @Override", //
        "  public TestNonEmptyConstructor createInstance(Injector injector) {", //
        "    String param1 = injector.getInstance(String.class);", //
        "    Integer param2 = injector.getInstance(Integer.class);", //
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

  @Test public void testProducesSingletonAnnotation() {
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
        "import toothpick.Injector;", //
        "", //
        "public final class TestNonEmptyConstructor$$Factory implements Factory<TestNonEmptyConstructor> {", //
        "  @Override", //
        "  public TestNonEmptyConstructor createInstance(Injector injector) {", //
        "    String param1 = injector.getInstance(String.class);", //
        "    Integer param2 = injector.getInstance(Integer.class);", //
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
}
