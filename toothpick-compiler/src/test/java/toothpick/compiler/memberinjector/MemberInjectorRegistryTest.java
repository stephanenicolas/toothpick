package toothpick.compiler.memberinjector;

import com.google.common.base.Joiner;
import com.google.testing.compile.JavaFileObjects;
import java.util.Arrays;
import java.util.Collections;
import javax.tools.JavaFileObject;
import org.junit.Test;

import static com.google.common.truth.Truth.assert_;
import static com.google.testing.compile.JavaSourceSubjectFactory.javaSource;

public class MemberInjectorRegistryTest {
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
        "package toothpick;", //
        "", //
        "import java.lang.Class;", //
        "import toothpick.registries.memberinjector.AbstractMemberInjectorRegistry;", //
        "", //
        "public final class MemberInjectorRegistry extends AbstractMemberInjectorRegistry {", //
        "  public MemberInjectorRegistry() {", //
        "  }", //
        "", //
        "  public <T> MemberInjector<T> getMemberInjector(Class<T> clazz) {", //
        "    switch(clazz.getName()) {", //
        "      case (\"test.TestASimpleRegistry\"):", //
        "      return (MemberInjector<T>) new test.TestASimpleRegistry$$MemberInjector();", //
        "      default:", //
        "      return getMemberInjectorInChildrenRegistries(clazz);", //
        "    }", //
        "  }", //
        "}" //
    ));

    assert_().about(javaSource())
        .that(source)
        .processedWith(toothpick.compiler.memberinjector.ProcessorTestUtilities.memberInjectorProcessors("toothpick", Collections.EMPTY_LIST))
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
        "package toothpick;", //
        "", //
        "import java.lang.Class;", //
        "import toothpick.registries.memberinjector.AbstractMemberInjectorRegistry;", //
        "", //
        "public final class MemberInjectorRegistry extends AbstractMemberInjectorRegistry {", //
        "  public MemberInjectorRegistry() {", //
        "    addChildRegistry(new toothpick.MemberInjectorRegistry());", //
        "    addChildRegistry(new toothpick.MemberInjectorRegistry());", //
        "  }", //
        "", //
        "  public <T> MemberInjector<T> getMemberInjector(Class<T> clazz) {", //
        "    switch(clazz.getName()) {", //
        "      case (\"test.TestARegistry\"):", //
        "      return (MemberInjector<T>) new test.TestARegistry$$MemberInjector();", //
        "      default:", //
        "      return getMemberInjectorInChildrenRegistries(clazz);", //
        "    }", //
        "  }", //
        "}" //
    ));

    assert_().about(javaSource())
        .that(source)
        .processedWith(
            toothpick.compiler.memberinjector.ProcessorTestUtilities.memberInjectorProcessors("toothpick", Arrays.asList("toothpick", "toothpick")))
        .compilesWithoutError()
        .and()
        .generatesSources(expectedSource);
  }
}
