package toothpick.compiler.memberinjector;

import com.google.common.base.Joiner;
import com.google.testing.compile.JavaFileObjects;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;

import javax.tools.JavaFileObject;

import static com.google.common.truth.Truth.assert_;
import static com.google.testing.compile.JavaSourceSubjectFactory.javaSource;

public class ObfuscationFriendlyMemberInjectorRegistryTest {
  @Test
  public void testASimpleRegistry() {
    JavaFileObject source = JavaFileObjects.forSourceString("test.TestASimpleRegistry", Joiner.on('\n').join(//
        "package test;", //
        "import javax.inject.Inject;", //
        "public class TestASimpleRegistry {", //
        "  @Inject String s; ", //
        "}" //
    ));

    JavaFileObject expectedSource = JavaFileObjects.forSourceString("toothpick/MemberInjectorRegistry", Joiner.on('\n').join(//
            "package toothpick;",
                    "",
                    "import java.lang.Class;",
                    "import java.lang.Override;",
                    "import java.lang.String;",
                    "import java.util.HashMap;",
                    "import java.util.Map;",
                    "import toothpick.registries.memberinjector.AbstractMemberInjectorRegistry;",
                    "",
                    "public final class MemberInjectorRegistry extends AbstractMemberInjectorRegistry {",
                    "    private final Map<String, MemberInjector> factories = new HashMap<>();",
                    "",
                    "    public MemberInjectorRegistry() {",
                    "      factories.put(\"test.TestASimpleRegistry\", new test.TestASimpleRegistry$$MemberInjector());",
                    "    }",
                    "",
                    "    @Override",
                    "    public <T> MemberInjector<T> getMemberInjector(Class<T> clazz) {",
                    "        MemberInjector factory = factories.get(clazz.getName());",
                    "        if (factory != null) {",
                    "            return (MemberInjector<T>) factory;",
                    "        }",
                    "        return getMemberInjectorInChildrenRegistries(clazz);",
                    "    }",
                    "}"));

    assert_().about(javaSource())
        .that(source)
        .processedWith(ProcessorTestUtilities.memberInjectorProcessors("toothpick", Collections.EMPTY_LIST, true))
        .compilesWithoutError()
        .and()
        .generatesSources(expectedSource);
  }

  @Test
  public void testARegistry_withDependencies() {
    JavaFileObject source = JavaFileObjects.forSourceString("test.TestARegistry", Joiner.on('\n').join(//
        "package test;", //
        "import javax.inject.Inject;", //
        "public class TestARegistry {", //
        "  @Inject String s; ", //
        "}" //
    ));

    JavaFileObject expectedSource = JavaFileObjects.forSourceString("toothpick/MemberInjectorRegistry", Joiner.on('\n').join(//
            "package toothpick;",
                    "",
                    "import java.lang.Class;",
                    "import java.lang.Override;",
                    "import java.lang.String;",
                    "import java.util.HashMap;",
                    "import java.util.Map;",
                    "import toothpick.registries.memberinjector.AbstractMemberInjectorRegistry;",
                    "",
                    "public final class MemberInjectorRegistry extends AbstractMemberInjectorRegistry {",
                    "    private final Map<String, MemberInjector> factories = new HashMap<>();",
                    "",
                    "    public MemberInjectorRegistry() {",
                    "      addChildRegistry(new toothpick.MemberInjectorRegistry());",
                    "      addChildRegistry(new toothpick.MemberInjectorRegistry());",
                    "      factories.put(\"test.TestARegistry\", new test.TestARegistry$$MemberInjector());",
                    "    }",
                    "",
                    "    @Override",
                    "    public <T> MemberInjector<T> getMemberInjector(Class<T> clazz) {",
                    "        MemberInjector factory = factories.get(clazz.getName());",
                    "        if (factory != null) {",
                    "            return (MemberInjector<T>) factory;",
                    "        }",
                    "        return getMemberInjectorInChildrenRegistries(clazz);",
                    "    }",
                    "}"));

    assert_().about(javaSource())
        .that(source)
        .processedWith(ProcessorTestUtilities.memberInjectorProcessors("toothpick", Arrays.asList("toothpick", "toothpick"), true))
        .compilesWithoutError()
        .and()
        .generatesSources(expectedSource);
  }

  @Test public void testARegistry_withNoFactories() {
    JavaFileObject source = JavaFileObjects.forSourceString("test.TestARegistryWithNoFactories", Joiner.on('\n').join(//
        "package test;", //
        "import javax.inject.Inject;", //
        "public class TestARegistryWithNoFactories {", //
        "  @Inject String s; ", //
        "}" //
    ));

    JavaFileObject expectedSource = JavaFileObjects.forSourceString("toothpick/MemberInjectorRegistry", Joiner.on('\n').join(//
            "package toothpick;",
                    "",
                    "import java.lang.Class;",
                    "import java.lang.Override;",
                    "import java.lang.String;",
                    "import java.util.HashMap;",
                    "import java.util.Map;",
                    "import toothpick.registries.memberinjector.AbstractMemberInjectorRegistry;",
                    "",
                    "public final class MemberInjectorRegistry extends AbstractMemberInjectorRegistry {",
                    "    private final Map<String, MemberInjector> factories = new HashMap<>();",
                    "",
                    "    public MemberInjectorRegistry() {",
                    "    }",
                    "",
                    "    @Override",
                    "    public <T> MemberInjector<T> getMemberInjector(Class<T> clazz) {",
                    "        MemberInjector factory = factories.get(clazz.getName());",
                    "        if (factory != null) {",
                    "            return (MemberInjector<T>) factory;",
                    "        }",
                    "        return getMemberInjectorInChildrenRegistries(clazz);",
                    "    }",
                    "}"));

    assert_().about(javaSource())
        .that(source)
        .processedWith(ProcessorTestUtilities.memberInjectorProcessors("toothpick", Collections.EMPTY_LIST, "java.*,android.*,test.*", true))
        .compilesWithoutError()
        .and()
        .generatesSources(expectedSource);
  }
}
