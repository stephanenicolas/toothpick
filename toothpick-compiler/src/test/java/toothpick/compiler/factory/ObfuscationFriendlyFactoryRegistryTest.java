package toothpick.compiler.factory;

import com.google.common.base.Joiner;
import com.google.testing.compile.JavaFileObjects;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;

import javax.tools.JavaFileObject;

import static com.google.common.truth.Truth.assert_;
import static com.google.testing.compile.JavaSourceSubjectFactory.javaSource;

public class ObfuscationFriendlyFactoryRegistryTest {
  @Test public void testASimpleRegistry() {
    JavaFileObject source = JavaFileObjects.forSourceString("test.TestASimpleRegistry", Joiner.on('\n').join(//
        "package test;", //
        "import javax.inject.Inject;", //
        "public class TestASimpleRegistry {", //
        "  @Inject public TestASimpleRegistry() {}", //
        "}" //
    ));

    JavaFileObject expectedSource = JavaFileObjects.forSourceString("toothpick/FactoryRegistry", Joiner.on('\n').join(
            "package toothpick;",
              "",
              "import java.lang.Class;",
              "import java.lang.Override;",
              "import java.lang.String;",
              "import java.util.HashMap;",
              "import java.util.Map;",
              "import toothpick.registries.factory.AbstractFactoryRegistry;",
              "",
              "public final class FactoryRegistry extends AbstractFactoryRegistry {",
              "    private final Map<String, Factory> factories = new HashMap<>();",
              "",
              "    public FactoryRegistry() {",
              "      factories.put(\"test.TestASimpleRegistry\", new test.TestASimpleRegistry$$Factory());",
              "    }",
              "",
              "    @Override",
              "    public <T> Factory<T> getFactory(Class<T> clazz) {",
              "        Factory factory = factories.get(clazz.getName());",
              "        if (factory != null) {",
              "            return (Factory<T>) factory;",
              "        }",
              "        return getFactoryInChildrenRegistries(clazz);",
              "    }",
              "}"
    ));

    assert_().about(javaSource())
        .that(source)
        .processedWith(ProcessorTestUtilities.factoryProcessors("toothpick", Collections.EMPTY_LIST, true))
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
            "package toothpick;",
                    "",
                    "import java.lang.Class;",
                    "import java.lang.Override;",
                    "import java.lang.String;",
                    "import java.util.HashMap;",
                    "import java.util.Map;",
                    "import toothpick.registries.factory.AbstractFactoryRegistry;",
                    "",
                    "public final class FactoryRegistry extends AbstractFactoryRegistry {",
                    "    private final Map<String, Factory> factories = new HashMap<>();",
                    "",
                    "    public FactoryRegistry() {",
                    "      addChildRegistry(new toothpick.FactoryRegistry());",
                    "      addChildRegistry(new toothpick.FactoryRegistry());",
                    "      factories.put(\"test.TestARegistry\", new test.TestARegistry$$Factory());",
                    "    }",
                    "",
                    "    @Override",
                    "    public <T> Factory<T> getFactory(Class<T> clazz) {",
                    "        Factory factory = factories.get(clazz.getName());",
                    "        if (factory != null) {",
                    "            return (Factory<T>) factory;",
                    "        }",
                    "        return getFactoryInChildrenRegistries(clazz);",
                    "    }",
                    "}"));

    assert_().about(javaSource())
        .that(source)
        .processedWith(ProcessorTestUtilities.factoryProcessors("toothpick", Arrays.asList("toothpick", "toothpick"), true))
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

    JavaFileObject expectedSource = JavaFileObjects.forSourceString("toothpick/FactoryRegistry", Joiner.on("\n").join(
            "package toothpick;",
                    "",
                    "import java.lang.Class;",
                    "import java.lang.Override;",
                    "import java.lang.String;",
                    "import java.util.HashMap;",
                    "import java.util.Map;",
                    "import toothpick.registries.factory.AbstractFactoryRegistry;",
                    "",
                    "public final class FactoryRegistry extends AbstractFactoryRegistry {",
                    "    private final Map<String, Factory> factories = new HashMap<>();",
                    "",
                    "    public FactoryRegistry() {",
                    "    }",
                    "",
                    "    @Override",
                    "    public <T> Factory<T> getFactory(Class<T> clazz) {",
                    "        Factory factory = factories.get(clazz.getName());",
                    "        if (factory != null) {",
                    "            return (Factory<T>) factory;",
                    "        }",
                    "        return getFactoryInChildrenRegistries(clazz);",
                    "    }",
                    "}"));

    assert_().about(javaSource())
        .that(source)
        .processedWith(ProcessorTestUtilities.factoryProcessors("toothpick",
                Collections.EMPTY_LIST, "java.*,android.*,test.*", true))
        .compilesWithoutError()
        .and()
        .generatesSources(expectedSource);
  }
}
