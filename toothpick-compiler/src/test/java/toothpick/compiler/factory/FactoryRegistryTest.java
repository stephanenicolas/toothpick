package toothpick.compiler.factory;

import com.google.common.base.Joiner;
import com.google.testing.compile.JavaFileObjects;
import java.util.Arrays;
import java.util.Collections;
import javax.tools.JavaFileObject;
import org.junit.Test;

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
        "import toothpick.registries.factory.AbstractFactoryRegistry;", //
        "", //
        "public final class FactoryRegistry extends AbstractFactoryRegistry {", //
        "  public FactoryRegistry() {", //
        "  }", //
        "", //
        "  public <T> Factory<T> getFactory(Class<T> clazz) {", //
        "    switch(clazz.getName().replace('$','.')) {", //
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
        "import toothpick.registries.factory.AbstractFactoryRegistry;", //
        "", //
        "public final class FactoryRegistry extends AbstractFactoryRegistry {", //
        "  public FactoryRegistry() {", //
        "    addChildRegistry(new toothpick.FactoryRegistry());", //
        "    addChildRegistry(new toothpick.FactoryRegistry());", //
        "  }", //
        "", //
        "  public <T> Factory<T> getFactory(Class<T> clazz) {", //
        "    switch(clazz.getName().replace('$','.')) {", //
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
}
