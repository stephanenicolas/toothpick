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
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.ElementFilter;
import toothpick.Factory;
import toothpick.compiler.common.ToothpickProcessor;
import toothpick.compiler.factory.generators.FactoryGenerator;
import toothpick.compiler.factory.targets.ConstructorInjectionTarget;
import toothpick.compiler.registry.generators.RegistryGenerator;
import toothpick.compiler.registry.targets.RegistryInjectionTarget;
import toothpick.registries.factory.AbstractFactoryRegistry;

import static java.lang.String.format;
import static javax.lang.model.element.Modifier.PRIVATE;
import static javax.lang.model.element.Modifier.PUBLIC;

//http://stackoverflow.com/a/2067863/693752
@SupportedAnnotationTypes({ ToothpickProcessor.INJECT_ANNOTATION_CLASS_NAME })
@SupportedOptions({
    ToothpickProcessor.PARAMETER_REGISTRY_PACKAGE_NAME, //
    ToothpickProcessor.PARAMETER_REGISTRY_CHILDREN_PACKAGE_NAMES, //
    ToothpickProcessor.PARAMETER_EXCLUDES
}) //

public class FactoryProcessor extends ToothpickProcessor {

  private Map<TypeElement, ConstructorInjectionTarget> mapTypeElementToConstructorInjectionTarget = new LinkedHashMap<>();

  @Override
  public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {

    if (toothpickRegistryPackageName == null) {
      readProcessorOptions();
    }
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
    if (toothpickRegistryPackageName != null || readProcessorOptions()) {
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
    for (ExecutableElement constructorElement : ElementFilter.constructorsIn(roundEnv.getElementsAnnotatedWith(Inject.class))) {
      TypeElement enclosingElement = (TypeElement) constructorElement.getEnclosingElement();

      if (!isSingleInjectedConstructor(constructorElement)) {
        error(constructorElement, "Class %s cannot have more than one @Inject annotated constructor.", enclosingElement.getQualifiedName());
      }

      parseInjectedConstructor(constructorElement, mapTypeElementToConstructorInjectionTarget);
    }
    //optimistically, we try to generate a factory for injected classes.
    //we want to alleviate the burden of creating @Inject constructors in trivially injected classes (those which
    //are bound to themselves, which is the default.
    //but we should process injected fields when they are of a class type,
    //not an interface. We could also create factories for them, if possible.
    //that would allow not to have to declare an annotation constructor in the
    //dependency. We would only use the default constructor.
    for (VariableElement fieldElement : ElementFilter.fieldsIn(roundEnv.getElementsAnnotatedWith(Inject.class))) {
      parseInjectedField(fieldElement, mapTypeElementToConstructorInjectionTarget);
      parseClassWithInjectedMembers(fieldElement.getEnclosingElement(), mapTypeElementToConstructorInjectionTarget);
    }
    //we do the same for all arguments of all methods
    for (ExecutableElement methodElement : ElementFilter.methodsIn(roundEnv.getElementsAnnotatedWith(Inject.class))) {
      parseInjectedMethod(methodElement, mapTypeElementToConstructorInjectionTarget);
      parseClassWithInjectedMembers(methodElement.getEnclosingElement(), mapTypeElementToConstructorInjectionTarget);
    }

    //TODO add singleton and produces singleton
  }

  private void parseClassWithInjectedMembers(Element enclosingElement,
      Map<TypeElement, ConstructorInjectionTarget> mapTypeElementToConstructorInjectionTarget) {
    final TypeElement typeElement = (TypeElement) typeUtils.asElement(enclosingElement.asType());
    if (mapTypeElementToConstructorInjectionTarget.containsKey(typeElement)) {
      //the class is already known
      return;
    }

    // Verify common generated code restrictions.
    if (!isValidInjectedType(typeElement)) {
      return;
    }

    ConstructorInjectionTarget constructorInjectionTarget = createConstructorInjectionTarget(typeElement);
    if (constructorInjectionTarget != null) {
      mapTypeElementToConstructorInjectionTarget.put(typeElement, constructorInjectionTarget);
    }
  }

  private boolean isSingleInjectedConstructor(Element constructorElement) {
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

  private void parseInjectedConstructor(ExecutableElement constructorElement, Map<TypeElement, ConstructorInjectionTarget> targetClassMap) {
    TypeElement enclosingElement = (TypeElement) constructorElement.getEnclosingElement();

    // Verify common generated code restrictions.
    if (!isValidInjectConstructor(constructorElement)) {
      return;
    }

    if (!isValidInjectedType(enclosingElement)) {
      return;
    }

    targetClassMap.put(enclosingElement, createConstructorInjectionTarget(constructorElement));

    //optimistic creation of factories for constructor param types
    parseInjectedParameters(constructorElement, mapTypeElementToConstructorInjectionTarget);
  }

  private void parseInjectedField(VariableElement fieldElement,
      Map<TypeElement, ConstructorInjectionTarget> mapTypeElementToConstructorInjectionTarget) {
    // Verify common generated code restrictions.
    if (!isValidInjectField(fieldElement)) {
      return;
    }

    final TypeElement fieldTypeElement = getType(fieldElement);
    if (mapTypeElementToConstructorInjectionTarget.containsKey(fieldTypeElement)) {
      //the class is already known
      return;
    }

    // Verify common generated code restrictions.
    if (!isValidInjectedType(fieldTypeElement)) {
      return;
    }

    ConstructorInjectionTarget constructorInjectionTarget = createConstructorInjectionTarget(fieldElement);
    if (constructorInjectionTarget != null) {
      mapTypeElementToConstructorInjectionTarget.put(fieldTypeElement, constructorInjectionTarget);
    }
  }

  private void parseInjectedMethod(ExecutableElement methodElement,
      Map<TypeElement, ConstructorInjectionTarget> mapTypeElementToConstructorInjectionTarget) {

    // Verify common generated code restrictions.
    if (!isValidInjectMethod(methodElement)) {
      return;
    }

    parseInjectedParameters(methodElement, mapTypeElementToConstructorInjectionTarget);
  }

  private void parseInjectedParameters(ExecutableElement methodElement,
      Map<TypeElement, ConstructorInjectionTarget> mapTypeElementToConstructorInjectionTarget) {
    for (VariableElement paramElement : methodElement.getParameters()) {
      final TypeElement paramTypeElement = getType(paramElement);

      if (mapTypeElementToConstructorInjectionTarget.containsKey(paramTypeElement)) {
        //the class is already known
        continue;
      }

      // Verify common generated code restrictions.
      if (!isValidInjectedType(paramTypeElement)) {
        continue;
      }

      ConstructorInjectionTarget constructorInjectionTarget = createConstructorInjectionTarget(paramElement);
      if (constructorInjectionTarget != null) {
        mapTypeElementToConstructorInjectionTarget.put(paramTypeElement, constructorInjectionTarget);
      }
    }
  }

  private boolean isValidInjectConstructor(Element element) {
    boolean valid = true;
    TypeElement enclosingElement = (TypeElement) element.getEnclosingElement();

    // Verify modifiers.
    Set<Modifier> modifiers = element.getModifiers();
    if (modifiers.contains(PRIVATE)) {
      error(element, "@Inject constructors must not be private in class %s.", enclosingElement.getQualifiedName());
      valid = false;
    }

    // Verify parentScope modifiers.
    Set<Modifier> parentModifiers = enclosingElement.getModifiers();
    //TODO should not be a non static inner class neither
    if (!parentModifiers.contains(PUBLIC)) {
      error(element, "Class %s is private. @Inject constructors are not allowed in non public classes.", enclosingElement.getQualifiedName());
      valid = false;
    }

    return valid;
  }

  private ConstructorInjectionTarget createConstructorInjectionTarget(ExecutableElement constructorElement) {
    TypeElement enclosingElement = (TypeElement) constructorElement.getEnclosingElement();
    final boolean hasSingletonAnnotation = hasAnnotationWithName(enclosingElement, "Singleton");
    final boolean hasProducesSingletonAnnotation = hasAnnotationWithName(enclosingElement, "ProvidesSingleton");
    TypeElement superClassWithInjectedMembers = getMostDirectSuperClassWithInjectedMembers(enclosingElement, false);

    ConstructorInjectionTarget constructorInjectionTarget =
        new ConstructorInjectionTarget(enclosingElement, hasSingletonAnnotation, hasProducesSingletonAnnotation, superClassWithInjectedMembers);
    constructorInjectionTarget.parameters.addAll(getParamInjectionTargetList(constructorElement));

    return constructorInjectionTarget;
  }

  private ConstructorInjectionTarget createConstructorInjectionTarget(TypeElement typeElement) {
    final boolean hasSingletonAnnotation = hasAnnotationWithName(typeElement, "Singleton");
    final boolean hasProducesSingletonAnnotation = hasAnnotationWithName(typeElement, "ProvidesSingleton");
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
          warning("The class %s has a private default constructor, toothpick can't optimistically create a factory for it.",
              typeElement.getQualifiedName().toString());
          return null;
        }

        ConstructorInjectionTarget constructorInjectionTarget =
            new ConstructorInjectionTarget(typeElement, hasSingletonAnnotation, hasProducesSingletonAnnotation, superClassWithInjectedMembers);
        return constructorInjectionTarget;
      }
    }

    warning("The class %s has no default constructor, toothpick can't optimistically create a factory for it.",
        typeElement.getQualifiedName().toString());
    return null;
  }

  private ConstructorInjectionTarget createConstructorInjectionTarget(VariableElement fieldElement) {
    final TypeElement fieldTypeElement = getType(fieldElement);
    return createConstructorInjectionTarget(fieldTypeElement);
  }

  private boolean isValidInjectedType(TypeElement fieldTypeElement) {
    System.out.println(fieldTypeElement.getQualifiedName().toString() + " isValid ?");
    if (isExcludedByFilters(fieldTypeElement)) return false;
    System.out.println(fieldTypeElement.getQualifiedName().toString() + " isValid");

    return !fieldTypeElement.getModifiers().contains(Modifier.ABSTRACT)
        //the previous line also covers && fieldTypeElement.getKind() != ElementKind.INTERFACE;
        && !fieldTypeElement.getModifiers().contains(Modifier.PRIVATE);
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
