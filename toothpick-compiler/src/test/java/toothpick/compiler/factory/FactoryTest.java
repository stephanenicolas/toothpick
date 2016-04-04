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

    JavaFileObject factorySource = JavaFileObjects.forSourceString("test/TestEmptyConstructor$$Factory", Joiner.on('\n').join(//
        "package test;", //
        "import java.lang.Override;", //
        "import toothpick.Factory;", //
        "import toothpick.Injector;", //
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
        .generatesSources(factorySource);
  }

  @Test public void testNonEmptyConstructor() {
    JavaFileObject source = JavaFileObjects.forSourceString("test.TestNonEmptyConstructor", Joiner.on('\n').join(//
        "package test;", //
        "import javax.inject.Inject;", //
        "public class TestNonEmptyConstructor {", //
        "  @Inject public TestNonEmptyConstructor(String str, Integer n) {}", //
        "}" //
    ));

    JavaFileObject factorySource = JavaFileObjects.forSourceString("test/TestEmptyNonConstructor$$Factory", Joiner.on('\n').join(//
        "package test;", //
        "import java.lang.Integer;", //
        "import java.lang.Override;", //
        "import java.lang.String;", //
        "import toothpick.Factory;", //
        "import toothpick.Injector;", //
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
        .generatesSources(factorySource);
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

    JavaFileObject factorySource = JavaFileObjects.forSourceString("test/TestEmptyNonConstructor$$Factory", Joiner.on('\n').join(//
        "package test;", //
        "import java.lang.Integer;", //
        "import java.lang.Override;", //
        "import java.lang.String;", //
        "import toothpick.Factory;", //
        "import toothpick.Injector;", //
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
        .generatesSources(factorySource);
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

    JavaFileObject factorySource = JavaFileObjects.forSourceString("test/TestEmptyNonConstructor$$Factory", Joiner.on('\n').join(//
        "package test;", //
        "import java.lang.Integer;", //
        "import java.lang.Override;", //
        "import java.lang.String;", //
        "import toothpick.Factory;", //
        "import toothpick.Injector;", //
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
        .generatesSources(factorySource);
  }
}
