package toothpick.compiler.memberinjector;

import com.google.common.base.Joiner;
import com.google.testing.compile.JavaFileObjects;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;

import javax.annotation.processing.Processor;
import javax.tools.JavaFileObject;

import static com.google.common.truth.Truth.assert_;
import static com.google.testing.compile.JavaSourceSubjectFactory.javaSource;

public class MemberInjectorRegistryTest {

  @Test
  public void testRegistry_withNoInjectors() {
    JavaFileObject source = classWithMemberInjection("Class0");

    JavaFileObject expectedSource = JavaFileObjects.forSourceString("toothpick/MemberInjectorRegistry", Joiner.on("\n").join(
            "package toothpick;", //
                    "", //
                    "import java.lang.Class;", //
                    "import java.lang.Integer;", //
                    "import java.lang.Override;", //
                    "import java.lang.String;", //
                    "import java.util.HashMap;", //
                    "import java.util.Map;", //
                    "import toothpick.registries.memberinjector.AbstractMemberInjectorRegistry;", //
                    "", //
                    "public final class MemberInjectorRegistry extends AbstractMemberInjectorRegistry {", //
                    "  private final Map<String, Integer> classNameToIndex = new HashMap<>();", //
                    "", //
                    "  public MemberInjectorRegistry() {", //
                    "  }", //
                    "", //
                    "  @Override", //
                    "  public <T> MemberInjector<T> getMemberInjector(Class<T> clazz) {", //
                    "    MemberInjector<T> factory = getFromThisRegistry(clazz);", //
                    "    if (factory == null) {", //
                    "      return getMemberInjectorInChildrenRegistries(clazz);", //
                    "    }", //
                    "    return factory;", //
                    "  }", //
                    "", //
                    "  private <T> MemberInjector<T> getFromThisRegistry(Class<T> clazz) {", //
                    "    return null;", //
                    "  }", //
                    "}"));

    assert_().about(javaSource())
            .that(source)
            .processedWith(ProcessorTestUtilities.memberInjectorProcessors("toothpick", Collections.EMPTY_LIST, "java.*,android.*,test.*"))
            .compilesWithoutError()
            .and()
            .generatesSources(expectedSource);
  }

  @Test
  public void testRegistry_WithOneInjector() {
    JavaFileObject source = classWithMemberInjection("Class0");

    JavaFileObject expectedSource = JavaFileObjects.forSourceString("toothpick/MemberInjectorRegistry", Joiner.on('\n').join(
                "package toothpick;", //
                "", //
                "import java.lang.Class;", //
                "import java.lang.Integer;", //
                "import java.lang.Override;", //
                "import java.lang.String;", //
                "import java.util.HashMap;", //
                "import java.util.Map;", //
                "import toothpick.registries.memberinjector.AbstractMemberInjectorRegistry;", //
                "", //
                "public final class MemberInjectorRegistry extends AbstractMemberInjectorRegistry {", //
                "  private final Map<String, Integer> classNameToIndex = new HashMap<>();", //
                "", //
                "  public MemberInjectorRegistry() {", //
                "    registerGroup0();", //
                "  }", //
                "", //
                "  private void registerGroup0() {", //
                "    classNameToIndex.put(\"test.Class0\", 0);", //
                "  }", //
                "", //
                "  @Override", //
                "  public <T> MemberInjector<T> getMemberInjector(Class<T> clazz) {", //
                "    MemberInjector<T> factory = getFromThisRegistry(clazz);", //
                "    if (factory == null) {", //
                "      return getMemberInjectorInChildrenRegistries(clazz);", //
                "    }", //
                "    return factory;", //
                "  }", //
                "", //
                "  private <T> MemberInjector<T> getFromThisRegistry(Class<T> clazz) {", //
                "    Integer index = classNameToIndex.get(clazz.getName());", //
                "    if (index == null) {", //
                "      return null;", //
                "    }", //
                "    int groupIndex = index / 200;", //
                "    switch(groupIndex) {", //
                "      case 0: return getFromGroup0(index);", //
                "    }", //
                "    return null;", //
                "  }", //
                "", //
                "  private <T> MemberInjector<T> getFromGroup0(int index) {", //
                "    switch(index) {", //
                "      case 0: return (MemberInjector<T>) new test.Class0$$MemberInjector();", //
                "    }", //
                "    return null;", //
                "  }", //
                "}"
    ));

    assert_().about(javaSource())
            .that(source)
            .processedWith(defaultMemberInjectorProcessor())
            .compilesWithoutError()
            .and()
            .generatesSources(expectedSource);
  }

  @Test
  public void testRegistry_withDependencies() {
    JavaFileObject source = classWithMemberInjection("Class0");
    JavaFileObject expectedSource = JavaFileObjects.forSourceString("toothpick/MemberInjectorRegistry", Joiner.on('\n').join(//
                "package toothpick;", //
                "", //
                "import java.lang.Class;", //
                "import java.lang.Integer;", //
                "import java.lang.Override;", //
                "import java.lang.String;", //
                "import java.util.HashMap;", //
                "import java.util.Map;", //
                "import toothpick.registries.memberinjector.AbstractMemberInjectorRegistry;", //
                "", //
                "public final class MemberInjectorRegistry extends AbstractMemberInjectorRegistry {", //
                "  private final Map<String, Integer> classNameToIndex = new HashMap<>();", //
                "", //
                "  public MemberInjectorRegistry() {", //
                "    addChildRegistry(new toothpick.MemberInjectorRegistry());", //
                "    addChildRegistry(new toothpick.MemberInjectorRegistry());", //
                "    registerGroup0();", //
                "  }", //
                "", //
                "  private void registerGroup0() {", //
                "    classNameToIndex.put(\"test.Class0\", 0);", //
                "  }", //
                "", //
                "  @Override", //
                "  public <T> MemberInjector<T> getMemberInjector(Class<T> clazz) {", //
                "    MemberInjector<T> factory = getFromThisRegistry(clazz);", //
                "    if (factory == null) {", //
                "      return getMemberInjectorInChildrenRegistries(clazz);", //
                "    }", //
                "    return factory;", //
                "  }", //
                "", //
                "  private <T> MemberInjector<T> getFromThisRegistry(Class<T> clazz) {", //
                "    Integer index = classNameToIndex.get(clazz.getName());", //
                "    if (index == null) {", //
                "      return null;", //
                "    }", //
                "    int groupIndex = index / 200;", //
                "    switch(groupIndex) {", //
                "      case 0: return getFromGroup0(index);", //
                "    }", //
                "    return null;", //
                "  }", //
                "", //
                "  private <T> MemberInjector<T> getFromGroup0(int index) {", //
                "    switch(index) {", //
                "      case 0: return (MemberInjector<T>) new test.Class0$$MemberInjector();", //
                "    }", //
                "    return null;", //
                "  }", //
                "}"));

    assert_().about(javaSource())
            .that(source)
            .processedWith(ProcessorTestUtilities.memberInjectorProcessors("toothpick", Arrays.asList("toothpick", "toothpick")))
            .compilesWithoutError()
            .and()
            .generatesSources(expectedSource);
  }

  private JavaFileObject classWithMemberInjection(String name) {
    return JavaFileObjects.forSourceString("test." + name, Joiner.on('\n').join(//
            "package test;", //
            "import javax.inject.Inject;", //
            "public class " + name + " {", //
            "  @Inject String s;", //
            "}" //
    ));
  }

  private Iterable<Processor> defaultMemberInjectorProcessor() {
    return ProcessorTestUtilities.memberInjectorProcessors("toothpick", Collections.EMPTY_LIST);
  }
}
