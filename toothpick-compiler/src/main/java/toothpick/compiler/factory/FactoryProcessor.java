package toothpick.compiler.factory;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedOptions;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.ElementFilter;
import toothpick.Factory;
import toothpick.ProvidesSingleton;
import toothpick.compiler.common.ToothpickProcessor;
import toothpick.compiler.factory.generators.FactoryGenerator;
import toothpick.compiler.factory.targets.ConstructorInjectionTarget;
import toothpick.compiler.registry.generators.RegistryGenerator;
import toothpick.compiler.registry.targets.RegistryInjectionTarget;
import toothpick.registries.factory.AbstractFactoryRegistry;

import static java.lang.String.format;
import static javax.lang.model.element.Modifier.PRIVATE;
import static javax.lang.model.element.Modifier.PUBLIC;

/**
 * This processor's role is to create {@link Factory}.
 * We create factories in different situations :
 * <ul>
 * <li> When a class {@code Foo} has an {@link javax.inject.Inject} annotated constructor : <br/>
 * --> we create a Factory to create {@code Foo} instances.
 * <li> When a class {@code Foo} is annotated with {@link javax.inject.Singleton} : <br/>
 * it will use the default constructor if possible, otherwise nothing happens. Should we raise an error ?
 * <li> When a class {@code Foo} is annotated with {@link toothpick.ProvidesSingleton} : <br/>
 * it will use the default constructor if possible, otherwise nothing happens. Should we raise an error ?
 * </ul>
 * The processor will also try to optimistically generate factories in a few cases. These factories
 * are optimistic as we don't know if the classes are really gonna be instanciated, but they are
 * helpful in most cases :
 * <ul>
 * <li> When a class {@code Foo} has an {@link javax.inject.Inject} constructor with param {@code B b} : <br/>
 * --> we create a Factory to create {@code B} instances.
 * <li> When a class {@code Foo} has an {@link javax.inject.Inject} annotated field {@code @Inject B b} : <br/>
 * --> we create a Factory to create {@code B} instances. <br/>
 * --> we create a Factory to create {@code Foo} instances. <br/>
 * <li> When a class {@code Foo} has an {@link javax.inject.Inject} method with param {@code B b} : <br/>
 * --> we create a Factory to create {@code B} instances. <br/>
 * --> we create a Factory to create {@code Foo} instances. <br/>
 * </ul>
 */
//http://stackoverflow.com/a/2067863/693752
@SupportedAnnotationTypes({
    ToothpickProcessor.INJECT_ANNOTATION_CLASS_NAME, //
    ToothpickProcessor.SINGLETON_ANNOTATION_CLASS_NAME, //
    ToothpickProcessor.PRODUCES_SINGLETON_ANNOTATION_CLASS_NAME
})
@SupportedOptions({
    ToothpickProcessor.PARAMETER_REGISTRY_PACKAGE_NAME, //
    ToothpickProcessor.PARAMETER_REGISTRY_CHILDREN_PACKAGE_NAMES, //
    ToothpickProcessor.PARAMETER_EXCLUDES
}) //
public class FactoryProcessor extends ToothpickProcessor {

  private Map<TypeElement, ConstructorInjectionTarget> mapTypeElementToConstructorInjectionTarget = new LinkedHashMap<>();

  @Override
  public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {

    readProcessorOptions();
    findAndParseTargets(roundEnv);

    if (!roundEnv.processingOver()) {
      return false;
    }

    // Generate Factories
    List<TypeElement> elementsWithFactoryCreated = new ArrayList<>();

    for (Map.Entry<TypeElement, ConstructorInjectionTarget> entry : mapTypeElementToConstructorInjectionTarget.entrySet()) {
      ConstructorInjectionTarget constructorInjectionTarget = entry.getValue();
      FactoryGenerator factoryGenerator = new FactoryGenerator(constructorInjectionTarget);
      TypeElement typeElement = entry.getKey();
      String fileDescription = format("Factory for type %s", typeElement);
      boolean success = writeToFile(factoryGenerator, fileDescription, typeElement);
      if (success) {
        elementsWithFactoryCreated.add(typeElement);
      }
    }

    // Generate Registry
    //this allows tests to by pass the option mechanism in processors
    if (toothpickRegistryPackageName != null) {
      RegistryInjectionTarget registryInjectionTarget =
          new RegistryInjectionTarget(Factory.class, AbstractFactoryRegistry.class, toothpickRegistryPackageName,
              toothpickRegistryChildrenPackageNameList, elementsWithFactoryCreated);
      RegistryGenerator registryGenerator = new RegistryGenerator(registryInjectionTarget);

      String fileDescription = "Factory registry";
      Element[] allTypes = elementsWithFactoryCreated.toArray(new Element[elementsWithFactoryCreated.size()]);
      writeToFile(registryGenerator, fileDescription, allTypes);
    }

    return false;
  }

  private void findAndParseTargets(RoundEnvironment roundEnv) {
    createFactoriesForClassesWithInjectAnnotatedConstructors(roundEnv);
    createFactoriesForClassesAnnotatedProvidesSingleton(roundEnv);
    createFactoriesForClassesAnnotatedSingleton(roundEnv);

    createOptimisticFactories(roundEnv);
  }

  private void createFactoriesForClassesAnnotatedProvidesSingleton(RoundEnvironment roundEnv) {
    for (Element singletonAnnotatedElement : roundEnv.getElementsAnnotatedWith(ProvidesSingleton.class)) {
      TypeElement singletonAnnotatedTypeElement = (TypeElement) singletonAnnotatedElement;
      processClassContainingInjectAnnotatedMember(singletonAnnotatedTypeElement, mapTypeElementToConstructorInjectionTarget);
    }
  }

  private void createFactoriesForClassesAnnotatedSingleton(RoundEnvironment roundEnv) {
    for (Element singletonAnnotatedElement : roundEnv.getElementsAnnotatedWith(Singleton.class)) {
      TypeElement singletonAnnotatedTypeElement = (TypeElement) singletonAnnotatedElement;
      processClassContainingInjectAnnotatedMember(singletonAnnotatedTypeElement, mapTypeElementToConstructorInjectionTarget);
    }
  }

  private void createFactoriesForClassesWithInjectAnnotatedConstructors(RoundEnvironment roundEnv) {
    for (ExecutableElement constructorElement : ElementFilter.constructorsIn(roundEnv.getElementsAnnotatedWith(Inject.class))) {
      TypeElement enclosingElement = (TypeElement) constructorElement.getEnclosingElement();

      if (!isSingleInjectAnnotatedConstructor(constructorElement)) {
        error(constructorElement, "Class %s cannot have more than one @Inject annotated constructor.", enclosingElement.getQualifiedName());
      }

      processInjectAnnotatedConstructor(constructorElement, mapTypeElementToConstructorInjectionTarget);
    }
  }

  /**
   * Optimistically, we try to generate a factory for classes containing injection related annotations.
   * We want to alleviate the burden of creating @Inject constructors in trivially injected classes (those which
   * are bound to themselves, those containing injected fields or methods, etc.
   * Factories are really needed for sure when a type is constructed by Toothpick, which means
   * when a class is used as the right part of a binging. But this is determined at runtime.
   * Hence, when possible, we create a factory "optimistically", for as many classes as we can.
   * We can't say at compile time if this factories are gonna be used, but there are many chances they will be used.
   * This allows not to have to declare an annotation constructor in the
   * dependencies. It makes using Toothpick easier.
   */
  private void createOptimisticFactories(RoundEnvironment roundEnv) {
    for (VariableElement fieldElement : ElementFilter.fieldsIn(roundEnv.getElementsAnnotatedWith(Inject.class))) {
      processInjectAnnotatedField(fieldElement, mapTypeElementToConstructorInjectionTarget);
      processClassContainingInjectAnnotatedMember(fieldElement.getEnclosingElement(), mapTypeElementToConstructorInjectionTarget);
    }
    for (ExecutableElement methodElement : ElementFilter.methodsIn(roundEnv.getElementsAnnotatedWith(Inject.class))) {
      processInjectAnnotatedMethod(methodElement, mapTypeElementToConstructorInjectionTarget);
      processClassContainingInjectAnnotatedMember(methodElement.getEnclosingElement(), mapTypeElementToConstructorInjectionTarget);
    }
  }

  private void processClassContainingInjectAnnotatedMember(Element enclosingElement,
      Map<TypeElement, ConstructorInjectionTarget> mapTypeElementToConstructorInjectionTarget) {
    final TypeElement typeElement = (TypeElement) typeUtils.asElement(enclosingElement.asType());
    if (mapTypeElementToConstructorInjectionTarget.containsKey(typeElement)) {
      //the class is already known
      return;
    }

    if (isExcludedByFilters(typeElement)) {
      return;
    }

    // Verify common generated code restrictions.
    if (!canTypeHaveAFactory(typeElement)) {
      return;
    }

    ConstructorInjectionTarget constructorInjectionTarget = createConstructorInjectionTarget(typeElement);
    if (constructorInjectionTarget != null) {
      mapTypeElementToConstructorInjectionTarget.put(typeElement, constructorInjectionTarget);
    }
  }

  private boolean isSingleInjectAnnotatedConstructor(Element constructorElement) {
    TypeElement enclosingElement = (TypeElement) constructorElement.getEnclosingElement();

    boolean isSingleInjectedConstructor = true;
    List<ExecutableElement> constructorElements = ElementFilter.constructorsIn(enclosingElement.getEnclosedElements());
    for (ExecutableElement constructorElementInClass : constructorElements) {
      if (constructorElementInClass.getAnnotation(Inject.class) != null && !constructorElement.equals(constructorElementInClass)) {
        isSingleInjectedConstructor = false;
      }
    }
    return isSingleInjectedConstructor;
  }

  private void processInjectAnnotatedConstructor(ExecutableElement constructorElement, Map<TypeElement, ConstructorInjectionTarget> targetClassMap) {
    TypeElement enclosingElement = (TypeElement) constructorElement.getEnclosingElement();

    // Verify common generated code restrictions.
    if (!isValidInjectAnnotatedConstructor(constructorElement)) {
      return;
    }

    if (isExcludedByFilters(enclosingElement)) {
      return;
    }

    if (!canTypeHaveAFactory(enclosingElement)) {
      error(enclosingElement, "The class %s is abstract or private. It cannot have an injected constructor.",
          enclosingElement.getQualifiedName());
      return;
    }

    targetClassMap.put(enclosingElement, createConstructorInjectionTarget(constructorElement));

    //optimistic creation of factories for constructor param types
    processInjectAnnotatedParameters(constructorElement, mapTypeElementToConstructorInjectionTarget);
  }

  private void processInjectAnnotatedField(VariableElement fieldElement,
      Map<TypeElement, ConstructorInjectionTarget> mapTypeElementToConstructorInjectionTarget) {
    // Verify common generated code restrictions.
    if (!isValidInjectAnnotatedFieldOrParameter(fieldElement)) {
      return;
    }

    final TypeElement fieldTypeElement = getInjectedType(fieldElement);
    if (mapTypeElementToConstructorInjectionTarget.containsKey(fieldTypeElement)) {
      //the class is already known
      return;
    }

    if (isExcludedByFilters(fieldTypeElement)) {
      return;
    }

    // Verify common generated code restrictions.
    if (!canTypeHaveAFactory(fieldTypeElement)) {
      return;
    }

    ConstructorInjectionTarget constructorInjectionTarget = createConstructorInjectionTarget(fieldTypeElement);
    if (constructorInjectionTarget != null) {
      mapTypeElementToConstructorInjectionTarget.put(fieldTypeElement, constructorInjectionTarget);
    }
  }

  private void processInjectAnnotatedMethod(ExecutableElement methodElement,
      Map<TypeElement, ConstructorInjectionTarget> mapTypeElementToConstructorInjectionTarget) {

    // Verify common generated code restrictions.
    if (!isValidInjectAnnotatedMethod(methodElement)) {
      return;
    }

    processInjectAnnotatedParameters(methodElement, mapTypeElementToConstructorInjectionTarget);
  }

  private void processInjectAnnotatedParameters(ExecutableElement methodElement,
      Map<TypeElement, ConstructorInjectionTarget> mapTypeElementToConstructorInjectionTarget) {
    for (VariableElement paramElement : methodElement.getParameters()) {
      final TypeElement paramTypeElement = getInjectedType(paramElement);

      if (mapTypeElementToConstructorInjectionTarget.containsKey(paramTypeElement)) {
        //the class is already known
        continue;
      }

      if (isExcludedByFilters(paramTypeElement)) {
        continue;
      }

      // Verify common generated code restrictions.
      if (!canTypeHaveAFactory(paramTypeElement)) {
        continue;
      }

      ConstructorInjectionTarget constructorInjectionTarget = createConstructorInjectionTarget(paramTypeElement);
      if (constructorInjectionTarget != null) {
        mapTypeElementToConstructorInjectionTarget.put(paramTypeElement, constructorInjectionTarget);
      }
    }
  }

  private boolean isValidInjectAnnotatedConstructor(Element element) {
    TypeElement enclosingElement = (TypeElement) element.getEnclosingElement();

    // Verify modifiers.
    Set<Modifier> modifiers = element.getModifiers();
    if (modifiers.contains(PRIVATE)) {
      error(element, "@Inject constructors must not be private in class %s.", enclosingElement.getQualifiedName());
      return false;
    }

    // Verify parentScope modifiers.
    Set<Modifier> parentModifiers = enclosingElement.getModifiers();
    if (!parentModifiers.contains(PUBLIC)) {
      error(element, "Class %s is private. @Inject constructors are not allowed in non public classes.", enclosingElement.getQualifiedName());
      return false;
    }

    if (isNonStaticInnerClass(enclosingElement)) {
      return false;
    }

    return true;
  }

  private ConstructorInjectionTarget createConstructorInjectionTarget(ExecutableElement constructorElement) {
    TypeElement enclosingElement = (TypeElement) constructorElement.getEnclosingElement();
    final boolean hasSingletonAnnotation = enclosingElement.getAnnotation(javax.inject.Singleton.class) != null;
    final boolean hasProducesSingletonAnnotation = enclosingElement.getAnnotation(toothpick.ProvidesSingleton.class) != null;
    TypeElement superClassWithInjectedMembers = getMostDirectSuperClassWithInjectedMembers(enclosingElement, false);

    ConstructorInjectionTarget constructorInjectionTarget =
        new ConstructorInjectionTarget(enclosingElement, hasSingletonAnnotation, hasProducesSingletonAnnotation, superClassWithInjectedMembers);
    constructorInjectionTarget.parameters.addAll(getParamInjectionTargetList(constructorElement));

    return constructorInjectionTarget;
  }

  private ConstructorInjectionTarget createConstructorInjectionTarget(TypeElement typeElement) {
    final boolean hasSingletonAnnotation = typeElement.getAnnotation(javax.inject.Singleton.class) != null;
    final boolean hasProducesSingletonAnnotation = typeElement.getAnnotation(toothpick.ProvidesSingleton.class) != null;
    TypeElement superClassWithInjectedMembers = getMostDirectSuperClassWithInjectedMembers(typeElement, false);

    List<ExecutableElement> constructorElements = ElementFilter.constructorsIn(typeElement.getEnclosedElements());
    //we just need to deal with the case of the default constructor only.
    //like Guice, we will call it by default in the optimistic factory
    //injected constructors will be handled at some point in the compilation cycle

    //if there is an injected constructor, it will be caught later, just leave
    for (ExecutableElement constructorElement : constructorElements) {
      if (constructorElement.getAnnotation(Inject.class) != null) {
        return null;
      }
    }

    //search for default constructor
    for (ExecutableElement constructorElement : constructorElements) {
      if (constructorElement.getParameters().isEmpty()) {
        if (constructorElement.getModifiers().contains(Modifier.PRIVATE)) {
          warning(constructorElement, "The class %s has a private default constructor, toothpick can't optimistically create a factory for it.",
              typeElement.getQualifiedName().toString());
          return null;
        }

        ConstructorInjectionTarget constructorInjectionTarget =
            new ConstructorInjectionTarget(typeElement, hasSingletonAnnotation, hasProducesSingletonAnnotation, superClassWithInjectedMembers);
        return constructorInjectionTarget;
      }
    }

    warning(typeElement, "The class %s has no default constructor, toothpick can't optimistically create a factory for it.",
        typeElement.getQualifiedName().toString());
    return null;
  }

  private boolean canTypeHaveAFactory(TypeElement typeElement) {
    boolean isAbstract = typeElement.getModifiers().contains(Modifier.ABSTRACT);
    boolean isPrivate = typeElement.getModifiers().contains(Modifier.PRIVATE);
    return !isAbstract && !isPrivate;
  }

  //used for testing only
  void setToothpickRegistryPackageName(String toothpickRegistryPackageName) {
    this.toothpickRegistryPackageName = toothpickRegistryPackageName;
  }

  //used for testing only
  void setToothpickRegistryChildrenPackageNameList(List<String> toothpickRegistryChildrenPackageNameList) {
    this.toothpickRegistryChildrenPackageNameList = toothpickRegistryChildrenPackageNameList;
  }
}
