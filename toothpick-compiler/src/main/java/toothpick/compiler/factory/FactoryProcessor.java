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
import javax.lang.model.util.ElementFilter;
import toothpick.compiler.ToothpickProcessor;

import static java.lang.String.format;
import static javax.lang.model.element.Modifier.PRIVATE;
import static javax.lang.model.element.Modifier.PUBLIC;

//http://stackoverflow.com/a/2067863/693752
@SupportedAnnotationTypes({ ToothpickProcessor.INJECT_ANNOTATION_CLASS_NAME })
@SupportedOptions({ ToothpickProcessor.PARAMETER_REGISTRY_PACKAGE_NAME + "." + ToothpickProcessor.PARAMETER_REGISTRY_CHILDREN_PACKAGE_NAMES }) //
public class FactoryProcessor extends ToothpickProcessor {

  private Map<TypeElement, FactoryInjectionTarget> mapTypeElementToFactoryInjectionTarget = new LinkedHashMap<>();

  @Override public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {

    findAndParseTargets(roundEnv);

    if (!roundEnv.processingOver()) {
      return false;
    }

    // Generate Factories
    for (Map.Entry<TypeElement, FactoryInjectionTarget> entry : mapTypeElementToFactoryInjectionTarget.entrySet()) {
      FactoryInjectionTarget factoryInjectionTarget = entry.getValue();
      FactoryGenerator factoryGenerator = new FactoryGenerator(factoryInjectionTarget);
      TypeElement typeElement = entry.getKey();
      String fileDescription = format("Factory for type %s", typeElement);
      writeToFile(factoryGenerator, fileDescription, typeElement);
    }

    // Generate Registry
    if (readParameters()) {
      FactoryRegistryInjectionTarget factoryRegistryInjectionTarget =
          new FactoryRegistryInjectionTarget(mapTypeElementToFactoryInjectionTarget.values(), toothpickRegistryPackageName,
              toothpickRegistryChildrenPackageNameList);
      FactoryRegistryGenerator factoryRegistryGenerator = new FactoryRegistryGenerator(factoryRegistryInjectionTarget);
      Element[] allTypes = mapTypeElementToFactoryInjectionTarget.keySet().toArray(new Element[mapTypeElementToFactoryInjectionTarget.size()]);
      String fileDescription = "Factory registry";
      writeToFile(factoryRegistryGenerator, fileDescription, allTypes);
    }

    return false;
  }

  private void findAndParseTargets(RoundEnvironment roundEnv) {

    //TODO we only process constructors
    //but we could also process injected fields when they are of a class type,
    //not an interface. We could also create factories for them, if possible.
    //that would allow not to have to declare an annotation constructor in the
    //dependency. We would only use the default constructor.
    for (Element element : ElementFilter.constructorsIn(roundEnv.getElementsAnnotatedWith(Inject.class))) {
      parseInjectedConstructor(element, mapTypeElementToFactoryInjectionTarget);
    }
  }

  private void parseInjectedConstructor(Element element, Map<TypeElement, FactoryInjectionTarget> targetClassMap) {
    TypeElement enclosingElement = (TypeElement) element.getEnclosingElement();

    // Verify common generated code restrictions.
    if (!isValidInjectConstructor(element)) {
      return;
    }

    if (targetClassMap.containsKey(enclosingElement)) {
      // Another constructor already used for the class.
      error(element, "Class %s cannot have more than one @Inject annotated constructor.", enclosingElement.getQualifiedName());
    }

    targetClassMap.put(enclosingElement, createInjectionTargetForConstructor(element));
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

  private FactoryInjectionTarget createInjectionTargetForConstructor(Element element) {
    TypeElement enclosingElement = (TypeElement) element.getEnclosingElement();
    final boolean hasSingletonAnnotation = hasAnnotationWithName(enclosingElement, "Singleton");
    final boolean hasProducesSingletonAnnotation = hasAnnotationWithName(enclosingElement, "ProvidesSingleton");
    boolean needsMemberInjection = needsMemberInjection(enclosingElement);

    FactoryInjectionTarget factoryInjectionTarget =
        new FactoryInjectionTarget(enclosingElement, hasSingletonAnnotation, hasProducesSingletonAnnotation, needsMemberInjection);
    addParameters(element, factoryInjectionTarget);

    return factoryInjectionTarget;
  }

  private boolean needsMemberInjection(TypeElement enclosingElement) {
    boolean needsMemberInjection = false;
    TypeElement currentTypeElement = enclosingElement;
    //TODO find a better test
    while (!"java.lang.Object".equals(currentTypeElement.getQualifiedName().toString())) {
      List<? extends Element> enclosedElements = currentTypeElement.getEnclosedElements();
      for (Element enclosedElement : enclosedElements) {
        if ((enclosedElement.getAnnotation(Inject.class) != null && enclosedElement.getKind() == ElementKind.FIELD) || (enclosedElement.getAnnotation(
            Inject.class) != null && enclosedElement.getKind() == ElementKind.METHOD)) {
          needsMemberInjection = true;
          break;
        }
      }
      currentTypeElement = (TypeElement) ((DeclaredType) currentTypeElement.getSuperclass()).asElement();
    }
    return needsMemberInjection;
  }

  private void addParameters(Element element, FactoryInjectionTarget factoryInjectionTarget) {
    ExecutableElement executableElement = (ExecutableElement) element;

    for (VariableElement variableElement : executableElement.getParameters()) {

      factoryInjectionTarget.parameters.add(variableElement.asType());
    }
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
