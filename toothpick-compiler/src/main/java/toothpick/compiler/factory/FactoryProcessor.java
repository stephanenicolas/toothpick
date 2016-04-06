package toothpick.compiler.factory;

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
import toothpick.compiler.ToothpickProcessor;
import toothpick.compiler.targets.ConstructorInjectionTarget;

import static java.lang.String.format;
import static javax.lang.model.element.Modifier.PRIVATE;
import static javax.lang.model.element.Modifier.PUBLIC;

//http://stackoverflow.com/a/2067863/693752
@SupportedAnnotationTypes({ ToothpickProcessor.INJECT_ANNOTATION_CLASS_NAME })
@SupportedOptions({ ToothpickProcessor.PARAMETER_REGISTRY_PACKAGE_NAME + "." + ToothpickProcessor.PARAMETER_REGISTRY_CHILDREN_PACKAGE_NAMES }) //
public class FactoryProcessor extends ToothpickProcessor {

  private Map<TypeElement, ConstructorInjectionTarget> mapTypeElementToConstructorInjectionTarget = new LinkedHashMap<>();

  @Override public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {

    findAndParseTargets(roundEnv);

    if (!roundEnv.processingOver()) {
      return false;
    }

    // Generate Factories
    for (Map.Entry<TypeElement, ConstructorInjectionTarget> entry : mapTypeElementToConstructorInjectionTarget.entrySet()) {
      ConstructorInjectionTarget constructorInjectionTarget = entry.getValue();
      FactoryGenerator factoryGenerator = new FactoryGenerator(constructorInjectionTarget);
      TypeElement typeElement = entry.getKey();
      String fileDescription = format("Factory for type %s", typeElement);
      writeToFile(factoryGenerator, fileDescription, typeElement);
    }

    // Generate Registry
    if (readParameters()) {
      FactoryRegistryInjectionTarget factoryRegistryInjectionTarget =
          new FactoryRegistryInjectionTarget(mapTypeElementToConstructorInjectionTarget.values(), toothpickRegistryPackageName,
              toothpickRegistryChildrenPackageNameList);
      FactoryRegistryGenerator factoryRegistryGenerator = new FactoryRegistryGenerator(factoryRegistryInjectionTarget);
      Element[] allTypes =
          mapTypeElementToConstructorInjectionTarget.keySet().toArray(new Element[mapTypeElementToConstructorInjectionTarget.size()]);
      String fileDescription = "Factory registry";
      writeToFile(factoryRegistryGenerator, fileDescription, allTypes);
    }

    return false;
  }

  private void findAndParseTargets(RoundEnvironment roundEnv) {
    for (ExecutableElement constructorElement : ElementFilter.constructorsIn(roundEnv.getElementsAnnotatedWith(Inject.class))) {
      TypeElement enclosingElement = (TypeElement) constructorElement.getEnclosingElement();

      if (!isSingleInjectedConstructor(constructorElement)) {
        error(constructorElement, "Class %s cannot have more than one @Inject annotated constructor.", enclosingElement.getQualifiedName());
      }

      //TODO add optimistic algorithm for all parameters of an injected constructor
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
      if (constructorElement.getAnnotation(Inject.class) != null && !constructorElement.equals(constructorElementInClass)) {
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

    targetClassMap.put(enclosingElement, createConstructorInjectionTargetForConstructor(constructorElement));
  }

  private void parseInjectedField(VariableElement fieldElement,
      Map<TypeElement, ConstructorInjectionTarget> mapTypeElementToConstructorInjectionTarget) {
    final TypeElement memberTypeElement = (TypeElement) typeUtils.asElement(fieldElement.asType());

    // Verify common generated code restrictions.
    if (!isValidInjectField(fieldElement)) {
      return;
    }

    if (mapTypeElementToConstructorInjectionTarget.containsKey(memberTypeElement)) {
      //the class is already known
      return;
    }

    final TypeElement fieldTypeElement = (TypeElement) typeUtils.asElement(fieldElement.asType());

    // Verify common generated code restrictions.
    if (!isValidInjectFieldType(fieldTypeElement)) {
      return;
    }

    ConstructorInjectionTarget constructorInjectionTargetForField = createConstructorInjectionTargetForVariableElement(fieldElement);
    if (constructorInjectionTargetForField != null) {
      mapTypeElementToConstructorInjectionTarget.put(memberTypeElement, constructorInjectionTargetForField);
    }
  }

  private void parseInjectedMethod(ExecutableElement methodElement,
      Map<TypeElement, ConstructorInjectionTarget> mapTypeElementToConstructorInjectionTarget) {
    final TypeElement memberTypeElement = (TypeElement) typeUtils.asElement(methodElement.asType());

    // Verify common generated code restrictions.
    if (!isValidInjectMethod(methodElement)) {
      return;
    }

    if (mapTypeElementToConstructorInjectionTarget.containsKey(memberTypeElement)) {
      //the class is already known
      return;
    }

    for (VariableElement paramElement : methodElement.getParameters()) {
      final TypeElement paramTypeElement = (TypeElement) typeUtils.asElement(paramElement.asType());
      // Verify common generated code restrictions.
      //TODO rename if this method still applies
      if (!isValidInjectFieldType(paramTypeElement)) {
        return;
      }

      ConstructorInjectionTarget constructorInjectionTargetForField = createConstructorInjectionTargetForVariableElement(paramElement);
      if (constructorInjectionTargetForField != null) {
        mapTypeElementToConstructorInjectionTarget.put(memberTypeElement, constructorInjectionTargetForField);
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

    // Verify parent modifiers.
    Set<Modifier> parentModifiers = enclosingElement.getModifiers();
    //TODO should not be a non static inner class neither
    if (!parentModifiers.contains(PUBLIC)) {
      error(element, "Class %s is private. @Inject constructors are not allowed in non public classes.", enclosingElement.getQualifiedName());
      valid = false;
    }

    return valid;
  }

  private ConstructorInjectionTarget createConstructorInjectionTargetForConstructor(ExecutableElement constructorElement) {
    TypeElement enclosingElement = (TypeElement) constructorElement.getEnclosingElement();
    final boolean hasSingletonAnnotation = hasAnnotationWithName(enclosingElement, "Singleton");
    final boolean hasProducesSingletonAnnotation = hasAnnotationWithName(enclosingElement, "ProvidesSingleton");
    boolean needsMemberInjection = needsMemberInjection(enclosingElement);

    ConstructorInjectionTarget constructorInjectionTarget =
        new ConstructorInjectionTarget(enclosingElement, hasSingletonAnnotation, hasProducesSingletonAnnotation, needsMemberInjection);
    constructorInjectionTarget.parameters.addAll(addParameters(constructorElement));

    return constructorInjectionTarget;
  }

  private ConstructorInjectionTarget createConstructorInjectionTargetForVariableElement(VariableElement fieldElement) {
    final TypeElement fieldTypeElement = (TypeElement) typeUtils.asElement(fieldElement.asType());

    final boolean hasSingletonAnnotation = hasAnnotationWithName(fieldTypeElement, "Singleton");
    final boolean hasProducesSingletonAnnotation = hasAnnotationWithName(fieldTypeElement, "ProvidesSingleton");
    boolean needsMemberInjection = needsMemberInjection(fieldTypeElement);

    List<ExecutableElement> constructorElements = ElementFilter.constructorsIn(fieldTypeElement.getEnclosedElements());
    //we just need to deal with the case of the defaul constructor only.
    //multiple constructors are non-decidable states.
    //injected constructors will be handled at some point in the compilation cycle
    if (constructorElements.size() == 1) {
      ExecutableElement constructorElement = constructorElements.get(0);
      if (!constructorElement.getParameters().isEmpty()) {
        warning("The class %s has no default constructor, we cannot optimistically create a factory for it.",
            fieldTypeElement.getQualifiedName().toString());
      }
      if (constructorElement.getModifiers().contains(Modifier.PRIVATE)) {
        warning("The class %s has a private default constructor, we cannot optimistically create a factory for it.",
            fieldTypeElement.getQualifiedName().toString());
      }
      ConstructorInjectionTarget constructorInjectionTarget =
          new ConstructorInjectionTarget(fieldTypeElement, hasSingletonAnnotation, hasProducesSingletonAnnotation, needsMemberInjection);
      return constructorInjectionTarget;
    }

    return null;
  }

  private boolean isValidInjectFieldType(TypeElement fieldTypeElement) {
    //TODO we probably need more filtering here, like to filter out android / java classes.
    //for those devs should provide a provider.
    return !fieldTypeElement.getModifiers().contains(Modifier.ABSTRACT)
        && !fieldTypeElement.getModifiers().contains(Modifier.PRIVATE)
        && fieldTypeElement.getKind() != ElementKind.INTERFACE;
  }

  private boolean needsMemberInjection(TypeElement enclosingElement) {
    TypeElement currentTypeElement = enclosingElement;
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
