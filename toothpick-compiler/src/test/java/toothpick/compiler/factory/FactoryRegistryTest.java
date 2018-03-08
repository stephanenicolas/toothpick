package toothpick.compiler.factory;

import com.google.common.base.Joiner;
import com.google.testing.compile.JavaFileObjects;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.annotation.processing.Processor;
import javax.tools.JavaFileObject;

import toothpick.compiler.registry.generators.RegistryGeneratorGroupSizeRule;

import static com.google.common.truth.Truth.assert_;
import static com.google.testing.compile.JavaSourceSubjectFactory.javaSource;
import static com.google.testing.compile.JavaSourcesSubjectFactory.javaSources;

public class FactoryRegistryTest {

  @Rule
  public final TestRule groupSizeRule = new RegistryGeneratorGroupSizeRule(3);

  @Test
  public void testRegistry_withNoFactories() {
    JavaFileObject source = injectableClass("Class0");

    JavaFileObject expectedSource = JavaFileObjects.forSourceString("toothpick/FactoryRegistry", Joiner.on("\n").join(
                "package toothpick;", //
                "", //
                "import java.lang.Class;", //
                "import java.lang.Integer;", //
                "import java.lang.Override;", //
                "import java.lang.String;", //
                "import java.util.HashMap;", //
                "import java.util.Map;", //
                "import toothpick.registries.factory.AbstractFactoryRegistry;", //
                "", //
                "public final class FactoryRegistry extends AbstractFactoryRegistry {", //
                "  private final Map<String, Integer> classNameToIndex = new HashMap<>();", //
                "", //
                "  public FactoryRegistry() {", //
                "  }", //
                "", //
                "  @Override", //
                "  public <T> Factory<T> getFactory(Class<T> clazz) {", //
                "    Factory<T> factory = getFromThisRegistry(clazz);", //
                "    if (factory == null) {", //
                "      return getFactoryInChildrenRegistries(clazz);", //
                "    }", //
                "    return factory;", //
                "  }", //
                "", //
                "  private <T> Factory<T> getFromThisRegistry(Class<T> clazz) {", //
                "    return null;", //
                "  }", //
                "}"));

    assert_().about(javaSource())
            .that(source)
            .processedWith(ProcessorTestUtilities.factoryProcessors("toothpick",
                    Collections.EMPTY_LIST, "java.*,android.*,test.*"))
            .compilesWithoutError()
            .and()
            .generatesSources(expectedSource);
  }

  @Test
  public void testRegistry_WithOneFactory() {
    JavaFileObject source = injectableClass("Class0");

    JavaFileObject expectedSource = JavaFileObjects.forSourceString("toothpick/FactoryRegistry", Joiner.on('\n').join(
                "package toothpick;", //
                "", //
                "import java.lang.Class;", //
                "import java.lang.Integer;", //
                "import java.lang.Override;", //
                "import java.lang.String;", //
                "import java.util.HashMap;", //
                "import java.util.Map;", //
                "import toothpick.registries.factory.AbstractFactoryRegistry;", //
                "", //
                "public final class FactoryRegistry extends AbstractFactoryRegistry {", //
                "  private final Map<String, Integer> classNameToIndex = new HashMap<>();", //
                "", //
                "  public FactoryRegistry() {", //
                "    registerGroup0();", //
                "  }", //
                "", //
                "  private void registerGroup0() {", //
                "    classNameToIndex.put(\"test.Class0\", 0);", //
                "  }", //
                "", //
                "  @Override", //
                "  public <T> Factory<T> getFactory(Class<T> clazz) {", //
                "    Factory<T> factory = getFromThisRegistry(clazz);", //
                "    if (factory == null) {", //
                "      return getFactoryInChildrenRegistries(clazz);", //
                "    }", //
                "    return factory;", //
                "  }", //
                "", //
                "  private <T> Factory<T> getFromThisRegistry(Class<T> clazz) {", //
                "    Integer index = classNameToIndex.get(clazz.getName());", //
                "    if (index == null) {", //
                "      return null;", //
                "    }", //
                "    int groupIndex = index / 3;", //
                "    switch(groupIndex) {", //
                "      case 0: return getFromGroup0(index);", //
                "    }", //
                "    return null;", //
                "  }", //
                "", //
                "  private <T> Factory<T> getFromGroup0(int index) {", //
                "    switch(index) {", //
                "      case 0: return (Factory<T>) new test.Class0$$Factory();", //
                "    }", //
                "    return null;", //
                "  }", //
                "}"
    ));

    assert_().about(javaSource())
        .that(source)
        .processedWith(defaultFactoryProcessor())
        .compilesWithoutError()
        .and()
        .generatesSources(expectedSource);
  }

  @Test
  public void testRegistry_WithOneCompleteGroupOfFactories() {
    Iterable<JavaFileObject> sources = injectableClasses("Class0", "Class1", "Class2");

    JavaFileObject expectedSource = JavaFileObjects.forSourceString("toothpick/FactoryRegistry", Joiner.on('\n').join(
                "package toothpick;", //
                "", //
                "import java.lang.Class;", //
                "import java.lang.Integer;", //
                "import java.lang.Override;", //
                "import java.lang.String;", //
                "import java.util.HashMap;", //
                "import java.util.Map;", //
                "import toothpick.registries.factory.AbstractFactoryRegistry;", //
                "", //
                "public final class FactoryRegistry extends AbstractFactoryRegistry {", //
                "  private final Map<String, Integer> classNameToIndex = new HashMap<>();", //
                "", //
                "  public FactoryRegistry() {", //
                "    registerGroup0();", //
                "  }", //
                "", //
                "  private void registerGroup0() {", //
                "    classNameToIndex.put(\"test.Class0\", 0);", //
                "    classNameToIndex.put(\"test.Class1\", 1);", //
                "    classNameToIndex.put(\"test.Class2\", 2);", //
                "  }", //
                "", //
                "  @Override", //
                "  public <T> Factory<T> getFactory(Class<T> clazz) {", //
                "    Factory<T> factory = getFromThisRegistry(clazz);", //
                "    if (factory == null) {", //
                "      return getFactoryInChildrenRegistries(clazz);", //
                "    }", //
                "    return factory;", //
                "  }", //
                "", //
                "  private <T> Factory<T> getFromThisRegistry(Class<T> clazz) {", //
                "    Integer index = classNameToIndex.get(clazz.getName());", //
                "    if (index == null) {", //
                "      return null;", //
                "    }", //
                "    int groupIndex = index / 3;", //
                "    switch(groupIndex) {", //
                "      case 0: return getFromGroup0(index);", //
                "    }", //
                "    return null;", //
                "  }", //
                "", //
                "  private <T> Factory<T> getFromGroup0(int index) {", //
                "    switch(index) {", //
                "      case 0: return (Factory<T>) new test.Class0$$Factory();", //
                "      case 1: return (Factory<T>) new test.Class1$$Factory();", //
                "      case 2: return (Factory<T>) new test.Class2$$Factory();", //
                "    }", //
                "    return null;", //
                "  }", //
                "}"
    ));

    assert_().about(javaSources())
            .that(sources)
            .processedWith(defaultFactoryProcessor())
            .compilesWithoutError()
            .and()
            .generatesSources(expectedSource);
  }

  @Test
  public void testRegistry_WithOneComplete_AndOneIncomplete_GroupOfFactories() {
    Iterable<JavaFileObject> sources = injectableClasses("Class0", "Class1", "Class2", "Class3", "Class4");

    JavaFileObject expectedSource = JavaFileObjects.forSourceString("toothpick/FactoryRegistry", Joiner.on('\n').join(
                "package toothpick;", //
                "", //
                "import java.lang.Class;", //
                "import java.lang.Integer;", //
                "import java.lang.Override;", //
                "import java.lang.String;", //
                "import java.util.HashMap;", //
                "import java.util.Map;", //
                "import toothpick.registries.factory.AbstractFactoryRegistry;", //
                "", //
                "public final class FactoryRegistry extends AbstractFactoryRegistry {", //
                "  private final Map<String, Integer> classNameToIndex = new HashMap<>();", //
                "", //
                "  public FactoryRegistry() {", //
                "    registerGroup0();", //
                "    registerGroup1();", //
                "  }", //
                "", //
                "  private void registerGroup0() {", //
                "    classNameToIndex.put(\"test.Class0\", 0);", //
                "    classNameToIndex.put(\"test.Class1\", 1);", //
                "    classNameToIndex.put(\"test.Class2\", 2);", //
                "  }", //
                "", //
                "  private void registerGroup1() {", //
                "    classNameToIndex.put(\"test.Class3\", 3);", //
                "    classNameToIndex.put(\"test.Class4\", 4);", //
                "  }", //
                "", //
                "  @Override", //
                "  public <T> Factory<T> getFactory(Class<T> clazz) {", //
                "    Factory<T> factory = getFromThisRegistry(clazz);", //
                "    if (factory == null) {", //
                "      return getFactoryInChildrenRegistries(clazz);", //
                "    }", //
                "    return factory;", //
                "  }", //
                "", //
                "  private <T> Factory<T> getFromThisRegistry(Class<T> clazz) {", //
                "    Integer index = classNameToIndex.get(clazz.getName());", //
                "    if (index == null) {", //
                "      return null;", //
                "    }", //
                "    int groupIndex = index / 3;", //
                "    switch(groupIndex) {", //
                "      case 0: return getFromGroup0(index);", //
                "      case 1: return getFromGroup1(index);", //
                "    }", //
                "    return null;", //
                "  }", //
                "", //
                "  private <T> Factory<T> getFromGroup0(int index) {", //
                "    switch(index) {", //
                "      case 0: return (Factory<T>) new test.Class0$$Factory();", //
                "      case 1: return (Factory<T>) new test.Class1$$Factory();", //
                "      case 2: return (Factory<T>) new test.Class2$$Factory();", //
                "    }", //
                "    return null;", //
                "  }", //
                "", //
                "  private <T> Factory<T> getFromGroup1(int index) {", //
                "    switch(index) {", //
                "      case 3: return (Factory<T>) new test.Class3$$Factory();", //
                "      case 4: return (Factory<T>) new test.Class4$$Factory();", //
                "    }", //
                "    return null;", //
                "  }", //
                "}"
    ));

    assert_().about(javaSources())
            .that(sources)
            .processedWith(defaultFactoryProcessor())
            .compilesWithoutError()
            .and()
            .generatesSources(expectedSource);
  }

  @Test
  public void testRegistry_WithTwoComplete_GroupsOfFactories() {
    Iterable<JavaFileObject> sources = injectableClasses("Class0", "Class1", "Class2", "Class3", "Class4", "Class5");

    JavaFileObject expectedSource = JavaFileObjects.forSourceString("toothpick/FactoryRegistry", Joiner.on('\n').join(
                "package toothpick;", //
                "", //
                "import java.lang.Class;", //
                "import java.lang.Integer;", //
                "import java.lang.Override;", //
                "import java.lang.String;", //
                "import java.util.HashMap;", //
                "import java.util.Map;", //
                "import toothpick.registries.factory.AbstractFactoryRegistry;", //
                "", //
                "public final class FactoryRegistry extends AbstractFactoryRegistry {", //
                "  private final Map<String, Integer> classNameToIndex = new HashMap<>();", //
                "", //
                "  public FactoryRegistry() {", //
                "    registerGroup0();", //
                "    registerGroup1();", //
                "  }", //
                "", //
                "  private void registerGroup0() {", //
                "    classNameToIndex.put(\"test.Class0\", 0);", //
                "    classNameToIndex.put(\"test.Class1\", 1);", //
                "    classNameToIndex.put(\"test.Class2\", 2);", //
                "  }", //
                "", //
                "  private void registerGroup1() {", //
                "    classNameToIndex.put(\"test.Class3\", 3);", //
                "    classNameToIndex.put(\"test.Class4\", 4);", //
                "    classNameToIndex.put(\"test.Class5\", 5);", //
                "  }", //
                "", //
                "  @Override", //
                "  public <T> Factory<T> getFactory(Class<T> clazz) {", //
                "    Factory<T> factory = getFromThisRegistry(clazz);", //
                "    if (factory == null) {", //
                "      return getFactoryInChildrenRegistries(clazz);", //
                "    }", //
                "    return factory;", //
                "  }", //
                "", //
                "  private <T> Factory<T> getFromThisRegistry(Class<T> clazz) {", //
                "    Integer index = classNameToIndex.get(clazz.getName());", //
                "    if (index == null) {", //
                "      return null;", //
                "    }", //
                "    int groupIndex = index / 3;", //
                "    switch(groupIndex) {", //
                "      case 0: return getFromGroup0(index);", //
                "      case 1: return getFromGroup1(index);", //
                "    }", //
                "    return null;", //
                "  }", //
                "", //
                "  private <T> Factory<T> getFromGroup0(int index) {", //
                "    switch(index) {", //
                "      case 0: return (Factory<T>) new test.Class0$$Factory();", //
                "      case 1: return (Factory<T>) new test.Class1$$Factory();", //
                "      case 2: return (Factory<T>) new test.Class2$$Factory();", //
                "    }", //
                "    return null;", //
                "  }", //
                "", //
                "  private <T> Factory<T> getFromGroup1(int index) {", //
                "    switch(index) {", //
                "      case 3: return (Factory<T>) new test.Class3$$Factory();", //
                "      case 4: return (Factory<T>) new test.Class4$$Factory();", //
                "      case 5: return (Factory<T>) new test.Class5$$Factory();", //
                "    }", //
                "    return null;", //
                "  }", //
                "}"
    ));

    assert_().about(javaSources())
            .that(sources)
            .processedWith(defaultFactoryProcessor())
            .compilesWithoutError()
            .and()
            .generatesSources(expectedSource);
  }

  @Test
  public void testRegistry_withDependencies() {
    JavaFileObject source = injectableClass("Class0");
    JavaFileObject expectedSource = JavaFileObjects.forSourceString("toothpick/FactoryRegistry", Joiner.on('\n').join(//
                "package toothpick;", //
                "", //
                "import java.lang.Class;", //
                "import java.lang.Integer;", //
                "import java.lang.Override;", //
                "import java.lang.String;", //
                "import java.util.HashMap;", //
                "import java.util.Map;", //
                "import toothpick.registries.factory.AbstractFactoryRegistry;", //
                "", //
                "public final class FactoryRegistry extends AbstractFactoryRegistry {", //
                "  private final Map<String, Integer> classNameToIndex = new HashMap<>();", //
                "", //
                "  public FactoryRegistry() {", //
                "    addChildRegistry(new toothpick.FactoryRegistry());", //
                "    addChildRegistry(new toothpick.FactoryRegistry());", //
                "    registerGroup0();", //
                "  }", //
                "", //
                "  private void registerGroup0() {", //
                "    classNameToIndex.put(\"test.Class0\", 0);", //
                "  }", //
                "", //
                "  @Override", //
                "  public <T> Factory<T> getFactory(Class<T> clazz) {", //
                "    Factory<T> factory = getFromThisRegistry(clazz);", //
                "    if (factory == null) {", //
                "      return getFactoryInChildrenRegistries(clazz);", //
                "    }", //
                "    return factory;", //
                "  }", //
                "", //
                "  private <T> Factory<T> getFromThisRegistry(Class<T> clazz) {", //
                "    Integer index = classNameToIndex.get(clazz.getName());", //
                "    if (index == null) {", //
                "      return null;", //
                "    }", //
                "    int groupIndex = index / 3;", //
                "    switch(groupIndex) {", //
                "      case 0: return getFromGroup0(index);", //
                "    }", //
                "    return null;", //
                "  }", //
                "", //
                "  private <T> Factory<T> getFromGroup0(int index) {", //
                "    switch(index) {", //
                "      case 0: return (Factory<T>) new test.Class0$$Factory();", //
                "    }", //
                "    return null;", //
                "  }", //
                "}"));

    assert_().about(javaSource())
        .that(source)
        .processedWith(ProcessorTestUtilities.factoryProcessors("toothpick", Arrays.asList("toothpick", "toothpick")))
        .compilesWithoutError()
        .and()
        .generatesSources(expectedSource);
  }


  private Iterable<JavaFileObject> injectableClasses(String... names) {
    List<JavaFileObject> list = new ArrayList<>();
    for (String name : names) {
      list.add(injectableClass(name));
    }
    return list;
  }


  private JavaFileObject injectableClass(String name) {
    return JavaFileObjects.forSourceString("test." + name, Joiner.on('\n').join(//
            "package test;", //
            "import javax.inject.Inject;", //
            "public class " + name + " {", //
            "  @Inject public " + name + "() {}", //
            "}" //
    ));
  }

  private Iterable<Processor> defaultFactoryProcessor() {
    return ProcessorTestUtilities.factoryProcessors("toothpick", Collections.EMPTY_LIST);
  }
}
