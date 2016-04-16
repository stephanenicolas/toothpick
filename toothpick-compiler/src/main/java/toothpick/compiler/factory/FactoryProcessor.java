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
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import toothpick.Factory;
import toothpick.compiler.ToothpickProcessor;
import toothpick.compiler.factory.generators.FactoryGenerator;
import toothpick.compiler.factory.targets.FactoryInjectionTarget;
import toothpick.compiler.registry.generators.RegistryGenerator;
import toothpick.compiler.registry.targets.RegistryInjectionTarget;
import toothpick.registries.factory.AbstractFactoryRegistry;

import static java.lang.String.format;
import static javax.lang.model.element.Modifier.PRIVATE;
import static javax.lang.model.element.Modifier.PUBLIC;

//http://stackoverflow.com/a/2067863/693752
@SupportedAnnotationTypes({ ToothpickProcessor.INJECT_ANNOTATION_CLASS_NAME })
@SupportedOptions({
    ToothpickProcessor.PARAMETER_REGISTRY_PACKAGE_NAME, ToothpickProcessor.PARAMETER_REGISTRY_CHILDREN_PACKAGE_NAMES,
    ToothpickProcessor.PARAMETER_EXCLUDES
}) //
public class FactoryProcessor extends ToothpickProcessor {

  private Map<TypeElement, FactoryInjectionTarget> mapTypeElementToConstructorInjectionTarget = new LinkedHashMap<>();

  @Override
  public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {

    findAndParseTargets(roundEnv);

    if (!roundEnv.processingOver()) {
      return false;
    }

    // Generate Factories
    List<TypeElement> elementsWithFactoryCreated = new ArrayList<>();

    for (Map.Entry<TypeElement, FactoryInjectionTarget> entry : mapTypeElementToConstructorInjectionTarget.entrySet()) {
      FactoryInjectionTarget factoryInjectionTarget = entry.getValue();
      FactoryGenerator factoryGenerator = new FactoryGenerator(factoryInjectionTarget);
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
    }
    //we do the same for all arguments of all methods
    for (ExecutableElement methodElement : ElementFilter.methodsIn(roundEnv.getElementsAnnotatedWith(Inject.class))) {
      parseInjectedMethod(methodElement, mapTypeElementToConstructorInjectionTarget);
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

  private void parseInjectedConstructor(ExecutableElement constructorElement, Map<TypeElement, FactoryInjectionTarget> targetClassMap) {
    TypeElement enclosingElement = (TypeElement) constructorElement.getEnclosingElement();

    // Verify common generated code restrictions.
    if (!isValidInjectConstructor(constructorElement)) {
      return;
    }

    if (!isValidInjectedType(enclosingElement)) {
      return;
    }

    targetClassMap.put(enclosingElement, createConstructorInjectionTargetForConstructor(constructorElement));

    //optimistic creation of factories for constructor param types
    parseInjectedParameters(constructorElement, mapTypeElementToConstructorInjectionTarget);
  }

  private void parseInjectedField(VariableElement fieldElement, Map<TypeElement, FactoryInjectionTarget> mapTypeElementToConstructorInjectionTarget) {
    // Verify common generated code restrictions.
    if (!isValidInjectField(fieldElement)) {
      return;
    }

    final TypeElement fieldTypeElement = (TypeElement) typeUtils.asElement(fieldElement.asType());
    if (mapTypeElementToConstructorInjectionTarget.containsKey(fieldTypeElement)) {
      //the class is already known
      return;
    }

    // Verify common generated code restrictions.
    if (!isValidInjectedType(fieldTypeElement)) {
      return;
    }

    FactoryInjectionTarget factoryInjectionTargetForField = createConstructorInjectionTargetForVariableElement(fieldElement);
    if (factoryInjectionTargetForField != null) {
      mapTypeElementToConstructorInjectionTarget.put(fieldTypeElement, factoryInjectionTargetForField);
    }
  }

  private void parseInjectedMethod(ExecutableElement methodElement,
      Map<TypeElement, FactoryInjectionTarget> mapTypeElementToConstructorInjectionTarget) {

    // Verify common generated code restrictions.
    if (!isValidInjectMethod(methodElement)) {
      return;
    }

    parseInjectedParameters(methodElement, mapTypeElementToConstructorInjectionTarget);
  }

  private void parseInjectedParameters(ExecutableElement methodElement,
      Map<TypeElement, FactoryInjectionTarget> mapTypeElementToConstructorInjectionTarget) {
    for (VariableElement paramElement : methodElement.getParameters()) {
      final TypeElement paramTypeElement = (TypeElement) typeUtils.asElement(paramElement.asType());

      if (mapTypeElementToConstructorInjectionTarget.containsKey(paramTypeElement)) {
        //the class is already known
        return;
      }

      // Verify common generated code restrictions.
      if (!isValidInjectedType(paramTypeElement)) {
        return;
      }

      FactoryInjectionTarget factoryInjectionTargetForField = createConstructorInjectionTargetForVariableElement(paramElement);
      if (factoryInjectionTargetForField != null) {
        mapTypeElementToConstructorInjectionTarget.put(paramTypeElement, factoryInjectionTargetForField);
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

  private FactoryInjectionTarget createConstructorInjectionTargetForConstructor(ExecutableElement constructorElement) {
    TypeElement enclosingElement = (TypeElement) constructorElement.getEnclosingElement();
    final boolean hasSingletonAnnotation = hasAnnotationWithName(enclosingElement, "Singleton");
    final boolean hasProducesSingletonAnnotation = hasAnnotationWithName(enclosingElement, "ProvidesSingleton");
    TypeElement superClassWithInjectedMembers = getMostDirectSuperClassWithInjectedMembers(enclosingElement);

    FactoryInjectionTarget factoryInjectionTarget =
        new FactoryInjectionTarget(enclosingElement, hasSingletonAnnotation, hasProducesSingletonAnnotation, superClassWithInjectedMembers);
    factoryInjectionTarget.parameters.addAll(addParameters(constructorElement));

    return factoryInjectionTarget;
  }

  private FactoryInjectionTarget createConstructorInjectionTargetForVariableElement(VariableElement fieldElement) {
    final TypeElement fieldTypeElement = (TypeElement) typeUtils.asElement(fieldElement.asType());

    final boolean hasSingletonAnnotation = hasAnnotationWithName(fieldTypeElement, "Singleton");
    final boolean hasProducesSingletonAnnotation = hasAnnotationWithName(fieldTypeElement, "ProvidesSingleton");
    TypeElement superClassWithInjectedMembers = getMostDirectSuperClassWithInjectedMembers(fieldTypeElement);

    List<ExecutableElement> constructorElements = ElementFilter.constructorsIn(fieldTypeElement.getEnclosedElements());
    //we just need to deal with the case of the defaul constructor only.
    //multiple constructors are non-decidable states.
    //injected constructors will be handled at some point in the compilation cycle
    if (constructorElements.size() == 1) {
      ExecutableElement constructorElement = constructorElements.get(0);
      if (!constructorElement.getParameters().isEmpty()) {
        warning("The class %s has no default constructor, we cannot optimistically create a factory for it.",
            fieldTypeElement.getQualifiedName().toString());
        return null;
      }

      if (constructorElement.getModifiers().contains(Modifier.PRIVATE)) {
        warning("The class %s has a private default constructor, we cannot optimistically create a factory for it.",
            fieldTypeElement.getQualifiedName().toString());
        return null;
      }
      FactoryInjectionTarget factoryInjectionTarget =
          new FactoryInjectionTarget(fieldTypeElement, hasSingletonAnnotation, hasProducesSingletonAnnotation, superClassWithInjectedMembers);
      return factoryInjectionTarget;
    }

    return null;
  }

  private boolean isValidInjectedType(TypeElement fieldTypeElement) {
    if (isExcludedByFilters(fieldTypeElement)) return false;

    return !fieldTypeElement.getModifiers().contains(Modifier.ABSTRACT)
        //the previous line also covers && fieldTypeElement.getKind() != ElementKind.INTERFACE;
        && !fieldTypeElement.getModifiers().contains(Modifier.PRIVATE);
  }

  private boolean needsMemberInjection(TypeElement typeElement) {
    TypeElement currentTypeElement = typeElement;
    do {
      List<? extends Element> enclosedElements = currentTypeElement.getEnclosedElements();
      for (Element enclosedElement : enclosedElements) {
        if ((enclosedElement.getAnnotation(Inject.class) != null && enclosedElement.getKind() == ElementKind.FIELD) || (enclosedElement.getAnnotation(
            Inject.class) != null && enclosedElement.getKind() == ElementKind.METHOD)) {
          return true;
        }
      }
      TypeMirror superclass = currentTypeElement.getSuperclass();
      if (superclass.getKind() == TypeKind.DECLARED) {
        DeclaredType superType = (DeclaredType) superclass;
        currentTypeElement = (TypeElement) superType.asElement();
      } else {
        currentTypeElement = null;
      }
    } while (currentTypeElement != null);
    return false;
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
