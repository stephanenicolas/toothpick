package toothpick.compiler.factory;

import java.io.PrintWriter;
import java.io.StringWriter;
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
import toothpick.compiler.ToothpickProcessor;

import static javax.lang.model.element.Modifier.PRIVATE;

//TODO add a @Generated annotation on generated classes, the value is the name of the factory class
@SupportedAnnotationTypes({ "javax.inject.Inject" })
@SupportedOptions({ "toothpick_registry_package_name.toothpick_registry_children_package_names" }) //
public class FactoryProcessor extends ToothpickProcessor {

  private Map<TypeElement, FactoryInjectionTarget> targetClassMap = new LinkedHashMap<>();

  @Override public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {

    findAndParseTargets(roundEnv);

    if (!roundEnv.processingOver()) {
      return false;
    }

    // Generate Factories
    for (Map.Entry<TypeElement, FactoryInjectionTarget> entry : targetClassMap.entrySet()) {
      FactoryInjectionTarget factoryInjectionTarget = entry.getValue();
      FactoryGenerator factoryGenerator = new FactoryGenerator(factoryInjectionTarget);
      TypeElement typeElement = entry.getKey();
      String fileDescription = String.format("Factory for type %s", typeElement);
      writeToFile(factoryGenerator, fileDescription, typeElement);
    }

    // Generate Registry
    if (readParameters()) {
      FactoryRegistryInjectionTarget factoryRegistryInjectionTarget =
          new FactoryRegistryInjectionTarget(targetClassMap.values(), toothpickRegistryPackageName, toothpickRegistryChildrenPackageNameList);
      FactoryRegistryGenerator factoryRegistryGenerator = new FactoryRegistryGenerator(factoryRegistryInjectionTarget);
      Element[] allTypes = targetClassMap.keySet().toArray(new Element[targetClassMap.size()]);
      String fileDescription = "Factory registry";
      writeToFile(factoryRegistryGenerator, fileDescription, allTypes);
    }

    return false;
  }

  private void findAndParseTargets(RoundEnvironment roundEnv) {

    for (Element element : roundEnv.getElementsAnnotatedWith(Inject.class)) {
      //TODO we only process constructors
      //but we could also process injected fields when they are of a class type,
      //not an interface. We could also create factories for them, if possible.
      //that would allow not to have to declare an annotation constructor in the
      //dependency. We would only use the default constructor.
      if (element.getKind() == ElementKind.CONSTRUCTOR) {
        try {
          parseInject(element, targetClassMap);
        } catch (Exception e) {
          StringWriter stackTrace = new StringWriter();
          e.printStackTrace(new PrintWriter(stackTrace));
          error(element, "Unable to generate factory when parsing @Inject.\n\n%s", stackTrace.toString());
        }
      }
    }
  }

  private void parseInject(Element element, Map<TypeElement, FactoryInjectionTarget> targetClassMap) {
    TypeElement enclosingElement = (TypeElement) element.getEnclosingElement();

    // Verify common generated code restrictions.
    if (!isValidInjectConstructor(element)) {
      return;
    }

    // Another constructor already used for the class.
    if (targetClassMap.containsKey(enclosingElement)) {
      throw new IllegalStateException(
          String.format("@%s class %s must not have more than one " + "annotated constructor.", Inject.class.getSimpleName(),
              element.getSimpleName()));
    }

    targetClassMap.put(enclosingElement, createInjectionTarget(element));
  }

  private boolean isValidInjectConstructor(Element element) {
    boolean valid = true;
    TypeElement enclosingElement = (TypeElement) element.getEnclosingElement();

    // Verify modifiers.
    Set<Modifier> modifiers = element.getModifiers();
    if (modifiers.contains(PRIVATE)) {
      error(element, "@%s constructors must not be private. (%s)", Inject.class.getSimpleName(), enclosingElement.getQualifiedName());
      valid = false;
    }

    // Verify parent modifiers.
    Set<Modifier> parentModifiers = enclosingElement.getModifiers();
    //TODO should not be a non static inner class neither
    if (parentModifiers.contains(PRIVATE)) {
      error(element, "@%s class %s must not be private or static.", Inject.class.getSimpleName(), element.getSimpleName());
      valid = false;
    }

    return valid;
  }

  private FactoryInjectionTarget createInjectionTarget(Element element) {
    TypeElement enclosingElement = (TypeElement) element.getEnclosingElement();

    final String classPackage = getPackageName(enclosingElement);
    final String className = getClassName(enclosingElement, classPackage);
    final String targetClass = enclosingElement.getQualifiedName().toString();
    final boolean hasSingletonAnnotation = hasAnnotationWithName(enclosingElement, "Singleton");
    final boolean hasProducesSingletonAnnotation = hasAnnotationWithName(enclosingElement, "ProvidesSingleton");
    boolean needsMemberInjection = needsMemberInjection(enclosingElement);

    FactoryInjectionTarget factoryInjectionTarget =
        new FactoryInjectionTarget(classPackage, className, targetClass, hasSingletonAnnotation, hasProducesSingletonAnnotation,
            needsMemberInjection);
    addParameters(element, factoryInjectionTarget);

    return factoryInjectionTarget;
  }

  private boolean needsMemberInjection(TypeElement enclosingElement) {
    boolean needsMemberInjection = false;
    TypeElement currentTypeElement = enclosingElement;
    while (!"java.lang.Object".equals(currentTypeElement.getQualifiedName().toString())) {
      List<? extends Element> enclosedElements = currentTypeElement.getEnclosedElements();
      for (Element enclosedElement : enclosedElements) {
        if (enclosedElement.getAnnotation(Inject.class) != null && enclosedElement.getKind() == ElementKind.FIELD) {
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
}
