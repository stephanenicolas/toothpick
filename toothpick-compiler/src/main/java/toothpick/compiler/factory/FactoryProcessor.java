package toothpick.compiler.factory;

import java.lang.annotation.Annotation;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedOptions;
import javax.inject.Inject;
import javax.inject.Scope;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.ElementFilter;
import toothpick.Factory;
import toothpick.ProvidesSingletonInScope;
import toothpick.compiler.common.ToothpickProcessor;
import toothpick.compiler.factory.generators.FactoryGenerator;
import toothpick.compiler.factory.targets.ConstructorInjectionTarget;
import toothpick.compiler.registry.generators.RegistryGenerator;
import toothpick.compiler.registry.targets.RegistryInjectionTarget;
import toothpick.registries.factory.AbstractFactoryRegistry;

import static java.lang.String.format;
import static javax.lang.model.element.Modifier.PRIVATE;

/**
 * This processor's role is to create {@link Factory}.
 * We create factories in different situations :
 * <ul>
 * <li> When a class {@code Foo} has an {@link javax.inject.Inject} annotated constructor : <br/>
 * --> we create a Factory to create {@code Foo} instances.
 * </ul>
 * The processor will also try to relax the constraints to generate factories in a few cases. These factories
 * are helpful as they require less work from developers :
 * <ul>
 * <li> When a class {@code Foo} is annotated with {@link javax.inject.Singleton} : <br/>
 * --> it will use the annotated constructor or the default constructor if possible. Otherwise an error is raised.
 * <li> When a class {@code Foo} is annotated with {@link ProvidesSingletonInScope} : <br/>
 * --> it will use the annotated constructor or the default constructor if possible. Otherwise an error is raised.
 * <li> When a class {@code Foo} has an {@link javax.inject.Inject} annotated field {@code @Inject B b} : <br/>
 * --> it will use the annotated constructor or the default constructor if possible. Otherwise an error is raised.
 * <li> When a class {@code Foo} has an {@link javax.inject.Inject} method {@code @Inject m()} : <br/>
 * --> it will use the annotated constructor or the default constructor if possible. Otherwise an error is raised.
 * </ul>
 * Note that if a class is abstract, the relax mechanism doesn't generate a factory and raises no error.
 */
//http://stackoverflow.com/a/2067863/693752
@SupportedOptions({
    ToothpickProcessor.PARAMETER_REGISTRY_PACKAGE_NAME, //
    ToothpickProcessor.PARAMETER_REGISTRY_CHILDREN_PACKAGE_NAMES, //
    ToothpickProcessor.PARAMETER_EXCLUDES, //
    ToothpickProcessor.PARAMETER_ANNOTATION_TYPES, //
    ToothpickProcessor.PARAMETER_CRASH_WHEN_NO_FACTORY_CAN_BE_CREATED, //
}) //
public class FactoryProcessor extends ToothpickProcessor {

  private static final String SUPPRESS_WARNING_ANNOTATION_INJECTABLE_VALUE = "injectable";

  private Map<TypeElement, ConstructorInjectionTarget> mapTypeElementToConstructorInjectionTarget = new LinkedHashMap<>();
  private Boolean crashWhenNoFactoryCanBeCreated;

  @Override
  public Set<String> getSupportedAnnotationTypes() {
    supportedAnnotationTypes.add(ToothpickProcessor.INJECT_ANNOTATION_CLASS_NAME);
    supportedAnnotationTypes.add(ToothpickProcessor.SINGLETON_ANNOTATION_CLASS_NAME);
    supportedAnnotationTypes.add(ToothpickProcessor.PRODUCES_SINGLETON_ANNOTATION_CLASS_NAME);
    readOptionAnnotationTypes();
    return supportedAnnotationTypes;
  }

  @Override
  public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {

    if (hasAlreadyRun()) {
      return false;
    }

    wasRun();
    readCommonProcessorOptions();
    readCrashWhenNoFactoryCanBeCreatedOption();
    findAndParseTargets(roundEnv, annotations);


    // Generate Factories
    List<TypeElement> elementsWithFactoryCreated = new ArrayList<>();

    for (Map.Entry<TypeElement, ConstructorInjectionTarget> entry : mapTypeElementToConstructorInjectionTarget.entrySet()) {
      ConstructorInjectionTarget constructorInjectionTarget = entry.getValue();
      FactoryGenerator factoryGenerator = new FactoryGenerator(constructorInjectionTarget, typeUtils);
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

      String fileDescription = "Factory registry";
      Element[] allTypes = elementsWithFactoryCreated.toArray(new Element[elementsWithFactoryCreated.size()]);
      writeToFile(new RegistryGenerator(registryInjectionTarget, typeUtils), fileDescription, allTypes);
    }

    return false;
  }

  private void readCrashWhenNoFactoryCanBeCreatedOption() {
    Map<String, String> options = processingEnv.getOptions();
    if (crashWhenNoFactoryCanBeCreated == null) {
      crashWhenNoFactoryCanBeCreated = Boolean.parseBoolean(options.get(PARAMETER_CRASH_WHEN_NO_FACTORY_CAN_BE_CREATED));
    }
  }

  private void findAndParseTargets(RoundEnvironment roundEnv, Set<? extends TypeElement> annotations) {
    createFactoriesForClassesWithInjectAnnotatedConstructors(roundEnv);
    createFactoriesForClassesAnnotatedWith(roundEnv, ProvidesSingletonInScope.class);
    createFactoriesForClassesWithInjectAnnotatedFields(roundEnv);
    createFactoriesForClassesWithInjectAnnotatedMethods(roundEnv);
    createFactoriesForClassesAnnotatedWithScopeAnnotations(roundEnv, annotations);
  }

  private void createFactoriesForClassesAnnotatedWithScopeAnnotations(RoundEnvironment roundEnv, Set<? extends TypeElement> annotations) {
    for (TypeElement annotation : annotations) {
      if (annotation.getAnnotation(Scope.class) != null) {
        checkScopeAnnotationValidity(annotation);
        createFactoriesForClassesAnnotatedWith(roundEnv, annotation);
      }
    }
  }

  private void createFactoriesForClassesWithInjectAnnotatedMethods(RoundEnvironment roundEnv) {
    for (ExecutableElement methodElement : ElementFilter.methodsIn(roundEnv.getElementsAnnotatedWith(Inject.class))) {
      processClassContainingInjectAnnotatedMember(methodElement.getEnclosingElement(), mapTypeElementToConstructorInjectionTarget);
    }
  }

  private void createFactoriesForClassesWithInjectAnnotatedFields(RoundEnvironment roundEnv) {
    for (VariableElement fieldElement : ElementFilter.fieldsIn(roundEnv.getElementsAnnotatedWith(Inject.class))) {
      processClassContainingInjectAnnotatedMember(fieldElement.getEnclosingElement(), mapTypeElementToConstructorInjectionTarget);
    }
  }

  private void createFactoriesForClassesAnnotatedWith(RoundEnvironment roundEnv, Class<? extends Annotation> annotationClass) {
    for (Element singletonAnnotatedElement : ElementFilter.typesIn(roundEnv.getElementsAnnotatedWith(annotationClass))) {
      TypeElement singletonAnnotatedTypeElement = (TypeElement) singletonAnnotatedElement;
      processClassContainingInjectAnnotatedMember(singletonAnnotatedTypeElement, mapTypeElementToConstructorInjectionTarget);
    }
  }

  private void createFactoriesForClassesAnnotatedWith(RoundEnvironment roundEnv, TypeElement annotationType) {
    for (Element singletonAnnotatedElement : ElementFilter.typesIn(roundEnv.getElementsAnnotatedWith(annotationType))) {
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
      error(enclosingElement, "The class %s is abstract or private. It cannot have an injected constructor.", enclosingElement.getQualifiedName());
      return;
    }

    targetClassMap.put(enclosingElement, createConstructorInjectionTarget(constructorElement));
  }

  private boolean isValidInjectAnnotatedConstructor(ExecutableElement element) {
    TypeElement enclosingElement = (TypeElement) element.getEnclosingElement();

    // Verify modifiers.
    Set<Modifier> modifiers = element.getModifiers();
    if (modifiers.contains(PRIVATE)) {
      error(element, "@Inject constructors must not be private in class %s.", enclosingElement.getQualifiedName());
      return false;
    }

    // Verify parentScope modifiers.
    Set<Modifier> parentModifiers = enclosingElement.getModifiers();
    if (parentModifiers.contains(PRIVATE)) {
      error(element, "Class %s is private. @Inject constructors are not allowed in private classes.", enclosingElement.getQualifiedName());
      return false;
    }

    if (isNonStaticInnerClass(enclosingElement)) {
      return false;
    }

    for (VariableElement paramElement : element.getParameters()) {
      if (!isValidInjectedType(paramElement)) {
        return false;
      }
    }

    return true;
  }

  private ConstructorInjectionTarget createConstructorInjectionTarget(ExecutableElement constructorElement) {
    TypeElement enclosingElement = (TypeElement) constructorElement.getEnclosingElement();
    final String scopeName = getScopeName(enclosingElement);
    final boolean hasScopeInstancesAnnotation = enclosingElement //
        .getAnnotation(ProvidesSingletonInScope.class) != null;
    if (hasScopeInstancesAnnotation && scopeName == null) {
      error(enclosingElement, "The type %s uses @ProvidesSingletonInScope but doesn't have a scope annotation.",
          enclosingElement.getQualifiedName().toString());
    }
    TypeElement superClassWithInjectedMembers = getMostDirectSuperClassWithInjectedMembers(enclosingElement, false);

    ConstructorInjectionTarget constructorInjectionTarget =
        new ConstructorInjectionTarget(enclosingElement, scopeName, hasScopeInstancesAnnotation, superClassWithInjectedMembers);
    constructorInjectionTarget.parameters.addAll(getParamInjectionTargetList(constructorElement));
    constructorInjectionTarget.throwsThrowable = !constructorElement.getThrownTypes().isEmpty();

    return constructorInjectionTarget;
  }

  private ConstructorInjectionTarget createConstructorInjectionTarget(TypeElement typeElement) {
    final String scopeName = getScopeName(typeElement);
    final boolean hasScopeInstancesAnnotation = typeElement.getAnnotation(ProvidesSingletonInScope.class) != null;
    if (hasScopeInstancesAnnotation && scopeName == null) {
      error(typeElement, "The type %s uses @ProvidesSingletonInScope but doesn't have a scope annotation.",
          typeElement.getQualifiedName().toString());
    }
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

    final String cannotCreateAFactoryMessage = " Toothpick can't create a factory for it." //
        + " If this class is itself a DI entry point (i.e. you call TP.inject(this) at some point), " //
        + " then you can remove this warning by adding @SuppressWarnings(\"Injectable\") to the class." //
        + " A typical example is a class using injection to assign its fields, that calls TP.inject(this)," //
        + " but it needs a parameter for its constructor and this parameter is not injectable.";

    //search for default constructor
    for (ExecutableElement constructorElement : constructorElements) {
      if (constructorElement.getParameters().isEmpty()) {
        if (constructorElement.getModifiers().contains(Modifier.PRIVATE)) {
          if (!isInjectableWarningSuppressed(typeElement)) {
              String message = format("The class %s has a private default constructor. "
                      + cannotCreateAFactoryMessage, //
                  typeElement.getQualifiedName().toString());
            crashOrWarnWhenNoFactoryCanBeCreated(constructorElement, message);
          }
          return null;
        }

        ConstructorInjectionTarget constructorInjectionTarget =
            new ConstructorInjectionTarget(typeElement, scopeName, hasScopeInstancesAnnotation, superClassWithInjectedMembers);
        return constructorInjectionTarget;
      }
    }

    if (!isInjectableWarningSuppressed(typeElement)) {
      String message = format("The class %s has injected members or a scope annotation "
              + "but has no @Inject annotated (non-private) constructor " //
              + " nor a non-private default constructor. " + cannotCreateAFactoryMessage, //
          typeElement.getQualifiedName().toString());
      crashOrWarnWhenNoFactoryCanBeCreated(typeElement, message);
    }
    return null;
  }

  private void crashOrWarnWhenNoFactoryCanBeCreated(Element element, String message) {
    if (crashWhenNoFactoryCanBeCreated != null && crashWhenNoFactoryCanBeCreated) {
      error(element, message);
    } else {
      warning(element, message);
    }
  }

  /**
   * Lookup {@link javax.inject.Scope} annotated annotations to provide the name of the scope the {@code typeElement} belongs to.
   * The method logs an error if the {@code typeElement} has multiple scope annotations.
   *
   * @param typeElement the element for which a scope is to be found.
   * @return the scope of this {@code typeElement} or {@code null} if it has no scope annotations.
   */
  private String getScopeName(TypeElement typeElement) {
    String scopeName = null;
    for (AnnotationMirror annotationMirror : typeElement.getAnnotationMirrors()) {
      TypeElement annotationTypeElement = (TypeElement) annotationMirror.getAnnotationType().asElement();
      if (annotationTypeElement.getAnnotation(Scope.class) != null) {
        checkScopeAnnotationValidity(annotationTypeElement);
        if (scopeName != null) {
          error(typeElement, "Only one @Scope qualified annotation is allowed : %s", scopeName);
        }
        scopeName = annotationTypeElement.getQualifiedName().toString();
      }
    }

    return scopeName;
  }

  private void checkScopeAnnotationValidity(TypeElement annotation) {
    if (annotation.getAnnotation(Scope.class) == null) {
      error(annotation, "Scope Annotation %s does not contain Scope annotation.", annotation.getQualifiedName());
      return;
    }

    Retention retention = annotation.getAnnotation(Retention.class);
    if (retention == null || retention.value() != RetentionPolicy.RUNTIME) {
      error(annotation, "Scope Annotation %s does not have RUNTIME retention policy.", annotation.getQualifiedName());
    }
  }

  /**
   * Checks if the injectable warning is suppressed for the TypeElement,
   * through the usage of @SuppressWarning("Injectable").
   *
   * @param typeElement the element to check if the warning is suppressed.
   * @return true is the injectable warning is suppressed, false otherwise.
   */
  private boolean isInjectableWarningSuppressed(TypeElement typeElement) {
    return hasWarningSuppressed(typeElement, SUPPRESS_WARNING_ANNOTATION_INJECTABLE_VALUE);
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

  //used for testing only
  void setToothpickExcludeFilters(String toothpickExcludeFilters) {
    this.toothpickExcludeFilters = toothpickExcludeFilters;
  }

  void setCrashWhenNoFactoryCanBeCreated(boolean crashWhenNoFactoryCanBeCreated) {
    this.crashWhenNoFactoryCanBeCreated = crashWhenNoFactoryCanBeCreated;
  }
}
