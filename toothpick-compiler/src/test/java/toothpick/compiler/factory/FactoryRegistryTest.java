package toothpick.compiler.factory;

import com.google.common.base.Joiner;
import com.google.testing.compile.JavaFileObjects;
import java.util.Arrays;
import java.util.Collections;
import javax.tools.JavaFileObject;
import org.junit.Test;
import toothpick.compiler.registry.generators.RegistryGeneratorTestUtilities;

import static com.google.common.truth.Truth.assert_;
import static com.google.testing.compile.JavaSourceSubjectFactory.javaSource;

public class FactoryRegistryTest {
  @Test public void testASimpleRegistry() {
    JavaFileObject source = JavaFileObjects.forSourceString("test.TestASimpleRegistry", Joiner.on('\n').join(//
        "package test;", //
        "import javax.inject.Inject;", //
        "public class TestASimpleRegistry {", //
        "  @Inject public TestASimpleRegistry() {}", //
        "}" //
    ));

    JavaFileObject expectedSource = JavaFileObjects.forSourceString("toothpick/FactoryRegistry", Joiner.on('\n').join(//
        "package toothpick;", //
        "", //
        "import java.lang.Class;", //
        "import java.lang.String;", //
        "import toothpick.registries.factory.AbstractFactoryRegistry;", //
        "", //
        "public final class FactoryRegistry extends AbstractFactoryRegistry {", //
        "  public FactoryRegistry() {", //
        "  }", //
        "", //
        "  public <T> Factory<T> getFactory(Class<T> clazz) {", //
        "    String className = clazz.getName();", //
        "    int bucket = (className.hashCode() & 0);", //
        "    switch(bucket) {", //
        "      case (0):", //
        "      return getFactoryBucket0(clazz, className);", //
        "      default:", //
        "      return getFactoryInChildrenRegistries(clazz);", //
        "    }", //
        "  }", //
        "", //
        "  private <T> Factory<T> getFactoryBucket0(Class<T> clazz, String className) {", //
        "    switch(className) {", //
        "      case (\"test.TestASimpleRegistry\"):", //
        "      return (Factory<T>) new test.TestASimpleRegistry$$Factory();", //
        "      default:", //
        "      return getFactoryInChildrenRegistries(clazz);", //
        "    }", //
        "  }", //
        "}" //
    ));

    assert_().about(javaSource())
        .that(source)
        .processedWith(ProcessorTestUtilities.factoryProcessors("toothpick", Collections.EMPTY_LIST))
        .compilesWithoutError()
        .and()
        .generatesSources(expectedSource);
  }

  @Test public void testARegistry_withDependencies() {
    JavaFileObject source = JavaFileObjects.forSourceString("test.TestARegistry", Joiner.on('\n').join(//
        "package test;", //
        "import javax.inject.Inject;", //
        "public class TestARegistry {", //
        "  @Inject public TestARegistry() {}", //
        "}" //
    ));

    JavaFileObject expectedSource = JavaFileObjects.forSourceString("toothpick/FactoryRegistry", Joiner.on('\n').join(//
        "package toothpick;", //
        "", //
        "import java.lang.Class;", //
        "import java.lang.String;", //
        "import toothpick.registries.factory.AbstractFactoryRegistry;", //
        "", //
        "public final class FactoryRegistry extends AbstractFactoryRegistry {", //
        "  public FactoryRegistry() {", //
        "    addChildRegistry(new toothpick.FactoryRegistry());", //
        "    addChildRegistry(new toothpick.FactoryRegistry());", //
        "  }", //
        "", //
        "  public <T> Factory<T> getFactory(Class<T> clazz) {", //
        "    String className = clazz.getName();", //
        "    int bucket = (className.hashCode() & 0);", //
        "    switch(bucket) {", //
        "      case (0):", //
        "      return getFactoryBucket0(clazz, className);", //
        "      default:", //
        "      return getFactoryInChildrenRegistries(clazz);", //
        "    }", //
        "  }", //
        "", //
        "  private <T> Factory<T> getFactoryBucket0(Class<T> clazz, String className) {", //
        "    switch(className) {", //
        "      case (\"test.TestARegistry\"):", //
        "      return (Factory<T>) new test.TestARegistry$$Factory();", //
        "      default:", //
        "      return getFactoryInChildrenRegistries(clazz);", //
        "    }", //
        "  }", //
        "}" //
    ));

    assert_().about(javaSource())
        .that(source)
        .processedWith(ProcessorTestUtilities.factoryProcessors("toothpick", Arrays.asList("toothpick", "toothpick")))
        .compilesWithoutError()
        .and()
        .generatesSources(expectedSource);
  }

  @Test public void testARegistry_withNoFactories() {
    JavaFileObject source = JavaFileObjects.forSourceString("test.TestARegistryWithNoFactories", Joiner.on('\n').join(//
        "package test;", //
        "import javax.inject.Inject;", //
        "public class TestARegistryWithNoFactories {", //
        "  @Inject public TestARegistryWithNoFactories() {}", //
        "}" //
    ));

    JavaFileObject expectedSource = JavaFileObjects.forSourceString("toothpick/FactoryRegistry", Joiner.on('\n').join(//
        "package toothpick;", //
        "", //
        "import java.lang.Class;", //
        "import toothpick.registries.factory.AbstractFactoryRegistry;", //
        "", //
        "public final class FactoryRegistry extends AbstractFactoryRegistry {", //
        "  public FactoryRegistry() {", //
        "  }", //
        "", //
        "  public <T> Factory<T> getFactory(Class<T> clazz) {", //
        "    String className = clazz.getName();", //
        "    int bucket = (className.hashCode() & -1);", //
        "    switch(bucket) {", //
        "      default:", //
        "      return getFactoryInChildrenRegistries(clazz);", //
        "    }", //
        "  }", //
        "}" //
    ));

    assert_().about(javaSource())
        .that(source)
        .processedWith(ProcessorTestUtilities.factoryProcessors("toothpick", Collections.EMPTY_LIST, "java.*,android.*,test.*"))
        .compilesWithoutError()
        .and()
        .generatesSources(expectedSource);
  }

  @Test public void testARegistry_withMoreThanOneBucket() {
    JavaFileObject source = JavaFileObjects.forSourceString("test.TestARegistryWithMoreThanOneBucket", Joiner.on('\n').join(//
        "package test;", //
        "import javax.inject.Inject;", //
        "public class TestARegistryWithMoreThanOneBucket {", //
        "  @Inject public TestARegistryWithMoreThanOneBucket() {}", //
        "  public static class InnerClass1 {", //
        "    @Inject public InnerClass1() {", //
        "    }", //
        "  }", //
        "  public static class InnerClass2 {", //
        "    public static class InnerClass3 {", //
        "      @Inject public InnerClass3() {", //
        "      }", //
        "    }", //
        "  }", //
        "}" //
    ));

    JavaFileObject expectedSource = JavaFileObjects.forSourceString("toothpick/FactoryRegistry", Joiner.on('\n').join(//
        "package toothpick;", //
        "", //
        "import java.lang.Class;", //
        "import java.lang.String;", //
        "import toothpick.registries.factory.AbstractFactoryRegistry;", //
        "", //
        "public final class FactoryRegistry extends AbstractFactoryRegistry {", //
        "  public FactoryRegistry() {", //
        "  }", //
        "", //
        "  public <T> Factory<T> getFactory(Class<T> clazz) {", //
        "    String className = clazz.getName();", //
        "    int bucket = (className.hashCode() & 3);", //
        "    switch(bucket) {", //
        "      case (0):", //
        "      return getFactoryBucket0(clazz, className);", //
        "      case (1):", //
        "      return getFactoryBucket1(clazz, className);", //
        "      case (2):", //
        "      return getFactoryBucket2(clazz, className);", //
        "      case (3):", //
        "      return getFactoryBucket3(clazz, className);", //
        "      default:", //
        "      return getFactoryInChildrenRegistries(clazz);", //
        "    }", //
        "  }", //
        "", //
        "  private <T> Factory<T> getFactoryBucket0(Class<T> clazz, String className) {", //
        "    switch(className) {", //
        "      case (\"test.TestARegistryWithMoreThanOneBucket\"):", //
        "      return (Factory<T>) new test.TestARegistryWithMoreThanOneBucket$$Factory();", //
        "      default:", //
        "      return getFactoryInChildrenRegistries(clazz);", //
        "    }", //
        "  }", //
        "", //
        "  private <T> Factory<T> getFactoryBucket1(Class<T> clazz, String className) {", //
        "    switch(className) {", //
        "      case (\"test.TestARegistryWithMoreThanOneBucket$InnerClass2$InnerClass3\"):", //
        "      return (Factory<T>) new test.TestARegistryWithMoreThanOneBucket$InnerClass2$InnerClass3$$Factory();", //
        "      default:", //
        "      return getFactoryInChildrenRegistries(clazz);", //
        "    }", //
        "  }", //
        "", //
        "  private <T> Factory<T> getFactoryBucket2(Class<T> clazz, String className) {", //
        "    switch(className) {", //
        "      default:", //
        "      return getFactoryInChildrenRegistries(clazz);", //
        "    }", //
        "  }", //
        "", //
        "  private <T> Factory<T> getFactoryBucket3(Class<T> clazz, String className) {", //
        "    switch(className) {", //
        "      case (\"test.TestARegistryWithMoreThanOneBucket$InnerClass1\"):", //
        "      return (Factory<T>) new test.TestARegistryWithMoreThanOneBucket$InnerClass1$$Factory();", //
        "      default:", //
        "      return getFactoryInChildrenRegistries(clazz);", //
        "    }", //
        "  }", //
        "}" //
    ));

    RegistryGeneratorTestUtilities.setInjectionTarjetsPerGetterMethod(1);

    assert_().about(javaSource())
        .that(source)
        .processedWith(ProcessorTestUtilities.factoryProcessors("toothpick", Collections.EMPTY_LIST))
        .compilesWithoutError()
        .and()
        .generatesSources(expectedSource);
  }
}
