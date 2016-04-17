package toothpick.compiler.memberinjector;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedOptions;
import javax.inject.Inject;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import toothpick.MemberInjector;
import toothpick.compiler.ToothpickProcessor;
import toothpick.compiler.factory.FactoryProcessor;
import toothpick.compiler.memberinjector.generators.MemberInjectorGenerator;
import toothpick.compiler.memberinjector.targets.FieldInjectionTarget;
import toothpick.compiler.memberinjector.targets.MethodInjectionTarget;
import toothpick.compiler.registry.generators.RegistryGenerator;
import toothpick.compiler.registry.targets.RegistryInjectionTarget;
import toothpick.registries.memberinjector.AbstractMemberInjectorRegistry;

/**
 * Same as {@link FactoryProcessor} but for {@link MemberInjector} classes.
 * Annotation processor arguments are shared with the {@link FactoryProcessor}.
 *
 * @see FactoryProcessor
 */
//http://stackoverflow.com/a/2067863/693752
@SupportedAnnotationTypes({ ToothpickProcessor.INJECT_ANNOTATION_CLASS_NAME })
@SupportedOptions({
    ToothpickProcessor.PARAMETER_REGISTRY_PACKAGE_NAME, ToothpickProcessor.PARAMETER_REGISTRY_CHILDREN_PACKAGE_NAMES,
    ToothpickProcessor.PARAMETER_EXCLUDES
}) //
public class MemberInjectorProcessor extends ToothpickProcessor {

  private Map<TypeElement, List<FieldInjectionTarget>> mapTypeElementToFieldInjectorTargetList = new LinkedHashMap<>();
  private Map<TypeElement, List<MethodInjectionTarget>> mapTypeElementToMethodInjectorTargetList = new LinkedHashMap<>();
  private Map<TypeElement, TypeElement> mapTypeElementToSuperTypeElementThatNeedsInjection = new LinkedHashMap<>();

  @Override
  public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
    findAndParseTargets(roundEnv);

    if (!roundEnv.processingOver()) {
      return false;
    }

    // Generate member scopes
    Set<TypeElement> elementWithInjectionSet = new HashSet<>();
    elementWithInjectionSet.addAll(mapTypeElementToFieldInjectorTargetList.keySet());
    elementWithInjectionSet.addAll(mapTypeElementToMethodInjectorTargetList.keySet());
    List<TypeElement> elementsWithMemberInjectorCreated = new ArrayList<>();

    for (TypeElement typeElement : elementWithInjectionSet) {
      List<FieldInjectionTarget> fieldInjectionTargetList = mapTypeElementToFieldInjectorTargetList.get(typeElement);
      List<MethodInjectionTarget> methodInjectionTargetList = mapTypeElementToMethodInjectorTargetList.get(typeElement);
      TypeElement superClassThatNeedsInjection = mapTypeElementToSuperTypeElementThatNeedsInjection.get(typeElement);
      MemberInjectorGenerator memberInjectorGenerator =
          new MemberInjectorGenerator(typeElement, superClassThatNeedsInjection, fieldInjectionTargetList, methodInjectionTargetList);
      String fileDescription = String.format("MemberInjector for type %s", typeElement);
      boolean success = writeToFile(memberInjectorGenerator, fileDescription, typeElement);
      if (success) {
        elementsWithMemberInjectorCreated.add(typeElement);
      }
    }

    // Generate Registry
    //this allows tests to by pass the option mechanism in processors
    if (toothpickRegistryPackageName != null || readProcessorOptions()) {
      RegistryInjectionTarget registryInjectionTarget =
          new RegistryInjectionTarget(MemberInjector.class, AbstractMemberInjectorRegistry.class, toothpickRegistryPackageName,
              toothpickRegistryChildrenPackageNameList, elementsWithMemberInjectorCreated);
      RegistryGenerator registryGenerator = new RegistryGenerator(registryInjectionTarget);

      String fileDescription = "MemberInjector registry";
      Element[] allTypes = elementsWithMemberInjectorCreated.toArray(new Element[elementsWithMemberInjectorCreated.size()]);
      writeToFile(registryGenerator, fileDescription, allTypes);
    }

    return false;
  }

  private void findAndParseTargets(RoundEnvironment roundEnv) {
    parseInjectedFields(roundEnv);
    parseInjectedMethods(roundEnv);
  }

  protected void parseInjectedFields(RoundEnvironment roundEnv) {
    for (VariableElement element : ElementFilter.fieldsIn(roundEnv.getElementsAnnotatedWith(Inject.class))) {
      if (!isExcludedByFilters((TypeElement) element.getEnclosingElement())) {
        parseInjectedField(element, mapTypeElementToFieldInjectorTargetList);
      }
    }
  }

  protected void parseInjectedMethods(RoundEnvironment roundEnv) {
    for (ExecutableElement element : ElementFilter.methodsIn(roundEnv.getElementsAnnotatedWith(Inject.class))) {
      if (!isExcludedByFilters((TypeElement) element.getEnclosingElement())) {
        parseInjectedMethod(element, mapTypeElementToMethodInjectorTargetList);
      }
    }
  }

  private void parseInjectedField(VariableElement fieldElement,
      Map<TypeElement, List<FieldInjectionTarget>> mapTypeElementToMemberInjectorTargetList) {
    TypeElement enclosingElement = (TypeElement) fieldElement.getEnclosingElement();

    // Verify common generated code restrictions.
    if (!isValidInjectField(fieldElement)) {
      return;
    }

    List<FieldInjectionTarget> fieldInjectionTargetList = mapTypeElementToMemberInjectorTargetList.get(enclosingElement);
    if (fieldInjectionTargetList == null) {
      fieldInjectionTargetList = new ArrayList<>();
      mapTypeElementToMemberInjectorTargetList.put(enclosingElement, fieldInjectionTargetList);
    }

    mapTypeToMostDirectSuperTypeThatNeedsInjection(enclosingElement);
    fieldInjectionTargetList.add(createFieldInjectionTarget(fieldElement));
  }

  //TODO take overrides into account. If the method is an override, do not generate a call to it
  //it will be performed by the super class member scope already.
  private void parseInjectedMethod(ExecutableElement methodElement,
      Map<TypeElement, List<MethodInjectionTarget>> mapTypeElementToMemberInjectorTargetList) {
    TypeElement enclosingElement = (TypeElement) methodElement.getEnclosingElement();

    // Verify common generated code restrictions.
    if (!isValidInjectMethod(methodElement)) {
      return;
    }

    List<MethodInjectionTarget> methodInjectionTargetList = mapTypeElementToMemberInjectorTargetList.get(enclosingElement);
    if (methodInjectionTargetList == null) {
      methodInjectionTargetList = new ArrayList<>();
      mapTypeElementToMemberInjectorTargetList.put(enclosingElement, methodInjectionTargetList);
    }

    mapTypeToMostDirectSuperTypeThatNeedsInjection(enclosingElement);
    methodInjectionTargetList.add(createMethodInjectionTarget(methodElement));
  }

  private void mapTypeToMostDirectSuperTypeThatNeedsInjection(TypeElement enclosingElement) {
    TypeElement superClassWithInjectedMembers = getMostDirectSuperClassWithInjectedMembers(enclosingElement, true);
    mapTypeElementToSuperTypeElementThatNeedsInjection.put(enclosingElement, superClassWithInjectedMembers);
  }

  private FieldInjectionTarget createFieldInjectionTarget(VariableElement element) {
    final TypeElement memberTypeElement = (TypeElement) typeUtils.asElement(element.asType());
    final String memberName = element.getSimpleName().toString();

    FieldInjectionTarget.Kind kind = getKind(element);
    TypeElement kindParameterTypeElement;
    if (kind == FieldInjectionTarget.Kind.INSTANCE) {
      kindParameterTypeElement = null;
    } else {
      kindParameterTypeElement = getKindParameter(element);
    }

    Object name = findQualifierName(element);

    return new FieldInjectionTarget(memberTypeElement, memberName, kind, kindParameterTypeElement, name);
  }

  /**
   * Lookup both {@link javax.inject.Qualifier} and {@link javax.inject.Named}
   * to provide the name of an injection.
   *
   * @param element the element for which a qualifier is to be found.
   * @return the name of this element or null if it has no qualifier annotations.
   */
  private String findQualifierName(VariableElement element) {
    String name = null;
    if (element.getAnnotationMirrors().isEmpty()) {
      return name;
    }

    for (AnnotationMirror annotationMirror : element.getAnnotationMirrors()) {
      TypeElement annotationTypeElement = (TypeElement) annotationMirror.getAnnotationType().asElement();
      if (isSameType(annotationTypeElement, "javax.inject.Named")) {
        checkIfAlreadyHasName(element, name);
        name = getValueOfAnnotation(annotationMirror);
      } else if (isAnnotationPresent(annotationTypeElement, "javax.inject.Qualifier")) {
        checkIfAlreadyHasName(element, name);
        name = annotationTypeElement.getQualifiedName().toString();
      }
    }
    return name;
  }

  private boolean isAnnotationPresent(TypeElement annotationTypeElement, String annotationName) {
    for (AnnotationMirror annotationOfAnnotationTypeMirror : annotationTypeElement.getAnnotationMirrors()) {
      TypeElement annotationOfAnnotationTypeElement = (TypeElement) annotationOfAnnotationTypeMirror.getAnnotationType().asElement();
      if (isSameType(annotationOfAnnotationTypeElement, annotationName)) {
        return true;
      }
    }
    return false;
  }

  private boolean isSameType(TypeElement annotationTypeElement, String annotationTypeName) {
    return typeUtils.isSameType(annotationTypeElement.asType(), elementUtils.getTypeElement(annotationTypeName).asType());
  }

  private void checkIfAlreadyHasName(VariableElement element, Object name) {
    if (name != null) {
      error(element, "Only one javax.inject.Qualifier annotation is allowed to name injections.");
    }
  }

  private String getValueOfAnnotation(AnnotationMirror annotationMirror) {
    String result = null;
    for (Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> annotationParamEntry : annotationMirror.getElementValues().entrySet()) {
      if (annotationParamEntry.getKey().getSimpleName().contentEquals("value")) {
        result = annotationParamEntry.getValue().toString().replaceAll("\"", "");
      }
    }
    return result;
  }

  private MethodInjectionTarget createMethodInjectionTarget(ExecutableElement methodElement) {
    TypeElement enclosingElement = (TypeElement) methodElement.getEnclosingElement();

    final TypeElement returnType = (TypeElement) typeUtils.asElement(methodElement.getReturnType());
    final String methodName = methodElement.getSimpleName().toString();

    MethodInjectionTarget methodInjectionTarget = new MethodInjectionTarget(enclosingElement, methodName, returnType);
    methodInjectionTarget.parameters.addAll(addParameters(methodElement));

    return methodInjectionTarget;
  }

  private FieldInjectionTarget.Kind getKind(Element element) {
    TypeMirror elementTypeMirror = element.asType();
    String elementTypeName = typeUtils.erasure(elementTypeMirror).toString();
    if ("javax.inject.Provider".equals(elementTypeName)) {
      return FieldInjectionTarget.Kind.PROVIDER;
    } else if ("toothpick.Lazy".equals(elementTypeName)) {
      return FieldInjectionTarget.Kind.LAZY;
    } else {
      return FieldInjectionTarget.Kind.INSTANCE;
    }
  }

  private TypeElement getKindParameter(Element element) {
    TypeMirror elementTypeMirror = element.asType();
    TypeMirror firstParameterTypeMirror = ((DeclaredType) elementTypeMirror).getTypeArguments().get(0);
    return (TypeElement) typeUtils.asElement(firstParameterTypeMirror);
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
