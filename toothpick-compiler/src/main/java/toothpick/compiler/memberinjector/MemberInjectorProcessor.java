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
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.ElementFilter;
import toothpick.MemberInjector;
import toothpick.compiler.common.ToothpickProcessor;
import toothpick.compiler.memberinjector.generators.MemberInjectorGenerator;
import toothpick.compiler.memberinjector.targets.FieldInjectionTarget;
import toothpick.compiler.memberinjector.targets.MethodInjectionTarget;
import toothpick.compiler.registry.generators.RegistryGenerator;
import toothpick.compiler.registry.targets.RegistryInjectionTarget;
import toothpick.registries.memberinjector.AbstractMemberInjectorRegistry;

/**
 * This processor's role is to create {@link MemberInjector}.
 * We create factories in different situations :
 * <ul>
 * <li> When a class {@code Foo} has an {@link javax.inject.Singleton} annotated field : <br/>
 * --> we create a MemberInjector to inject {@code Foo} instances.
 * <li> When a class {@code Foo} has an {@link javax.inject.Singleton} method : <br/>
 * --> we create a MemberInjector to inject {@code Foo} instances.
 * </ul>
 */
//http://stackoverflow.com/a/2067863/693752
@SupportedAnnotationTypes({ ToothpickProcessor.INJECT_ANNOTATION_CLASS_NAME })
@SupportedOptions({
    ToothpickProcessor.PARAMETER_REGISTRY_PACKAGE_NAME, //
    ToothpickProcessor.PARAMETER_REGISTRY_CHILDREN_PACKAGE_NAMES, //
    ToothpickProcessor.PARAMETER_EXCLUDES, //
    ToothpickProcessor.PARAMETER_CRASH_WHEN_INJECTED_METHOD_IS_NOT_PACKAGE
}) //
public class MemberInjectorProcessor extends ToothpickProcessor {

  private Map<TypeElement, List<FieldInjectionTarget>> mapTypeElementToFieldInjectorTargetList = new LinkedHashMap<>();
  private Map<TypeElement, List<MethodInjectionTarget>> mapTypeElementToMethodInjectorTargetList = new LinkedHashMap<>();
  private Map<TypeElement, TypeElement> mapTypeElementToSuperTypeElementThatNeedsInjection = new LinkedHashMap<>();

  @Override
  public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {

    if (hasAlreadyRun()) {
      return false;
    }

    wasRun();
    readCommonProcessorOptions();
    readOptionCrashWhenMethodIsNotPackageProtected();
    findAndParseTargets(roundEnv);

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
          new MemberInjectorGenerator(typeElement, //
              superClassThatNeedsInjection, //
              fieldInjectionTargetList, //
              methodInjectionTargetList, //
              typeUtils);
      String fileDescription = String.format("MemberInjector for type %s", typeElement);
      boolean success = writeToFile(memberInjectorGenerator, fileDescription, typeElement);
      if (success) {
        elementsWithMemberInjectorCreated.add(typeElement);
      }
    }

    // Generate Registry
    if (toothpickRegistryPackageName != null) {
      RegistryInjectionTarget registryInjectionTarget =
          new RegistryInjectionTarget(MemberInjector.class, AbstractMemberInjectorRegistry.class, toothpickRegistryPackageName,
              toothpickRegistryChildrenPackageNameList, elementsWithMemberInjectorCreated);

      String fileDescription = "MemberInjector registry";
      Element[] allTypes = elementsWithMemberInjectorCreated.toArray(new Element[elementsWithMemberInjectorCreated.size()]);
      writeToFile(new RegistryGenerator(registryInjectionTarget, typeUtils), fileDescription, allTypes);
    }

    return false;
  }

  private void readOptionCrashWhenMethodIsNotPackageProtected() {
    Map<String, String> options = processingEnv.getOptions();
    if (toothpickCrashWhenMethodIsNotPackageVisible == null) {
      toothpickCrashWhenMethodIsNotPackageVisible =
          Boolean.parseBoolean(options.get(PARAMETER_CRASH_WHEN_INJECTED_METHOD_IS_NOT_PACKAGE));
    }
  }

  private void findAndParseTargets(RoundEnvironment roundEnv) {
    processInjectAnnotatedFields(roundEnv);
    processInjectAnnotatedMethods(roundEnv);
  }

  protected void processInjectAnnotatedFields(RoundEnvironment roundEnv) {
    for (VariableElement element : ElementFilter.fieldsIn(roundEnv.getElementsAnnotatedWith(Inject.class))) {
      if (!isExcludedByFilters((TypeElement) element.getEnclosingElement())) {
        processInjectAnnotatedField(element, mapTypeElementToFieldInjectorTargetList);
      }
    }
  }

  protected void processInjectAnnotatedMethods(RoundEnvironment roundEnv) {
    for (ExecutableElement element : ElementFilter.methodsIn(roundEnv.getElementsAnnotatedWith(Inject.class))) {
      if (!isExcludedByFilters((TypeElement) element.getEnclosingElement())) {
        processInjectAnnotatedMethod(element, mapTypeElementToMethodInjectorTargetList);
      }
    }
  }

  private void processInjectAnnotatedField(VariableElement fieldElement,
      Map<TypeElement, List<FieldInjectionTarget>> mapTypeElementToMemberInjectorTargetList) {
    TypeElement enclosingElement = (TypeElement) fieldElement.getEnclosingElement();

    // Verify common generated code restrictions.
    if (!isValidInjectAnnotatedFieldOrParameter(fieldElement)) {
      return;
    }

    List<FieldInjectionTarget> fieldInjectionTargetList = mapTypeElementToMemberInjectorTargetList.get(enclosingElement);
    if (fieldInjectionTargetList == null) {
      fieldInjectionTargetList = new ArrayList<>();
      mapTypeElementToMemberInjectorTargetList.put(enclosingElement, fieldInjectionTargetList);
    }

    mapTypeToMostDirectSuperTypeThatNeedsInjection(enclosingElement);
    fieldInjectionTargetList.add(createFieldOrParamInjectionTarget(fieldElement));
  }

  private void processInjectAnnotatedMethod(ExecutableElement methodElement,
      Map<TypeElement, List<MethodInjectionTarget>> mapTypeElementToMemberInjectorTargetList) {
    TypeElement enclosingElement = (TypeElement) methodElement.getEnclosingElement();

    // Verify common generated code restrictions.
    if (!isValidInjectAnnotatedMethod(methodElement)) {
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

  private void mapTypeToMostDirectSuperTypeThatNeedsInjection(TypeElement typeElement) {
    TypeElement superClassWithInjectedMembers = getMostDirectSuperClassWithInjectedMembers(typeElement, true);
    mapTypeElementToSuperTypeElementThatNeedsInjection.put(typeElement, superClassWithInjectedMembers);
  }

  private MethodInjectionTarget createMethodInjectionTarget(ExecutableElement methodElement) {
    TypeElement enclosingElement = (TypeElement) methodElement.getEnclosingElement();

    final String methodName = methodElement.getSimpleName().toString();

    boolean isOverride = isOverride(enclosingElement, methodElement);

    MethodInjectionTarget methodInjectionTarget = new MethodInjectionTarget(enclosingElement, methodName, isOverride);
    methodInjectionTarget.parameters.addAll(getParamInjectionTargetList(methodElement));

    return methodInjectionTarget;
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

  //used for testing only
  void setCrashOrWarnWhenMethodIsNotPackageVisible(boolean crashOrWarnWhenMethodIsNotPackageVisible) {
    this.toothpickCrashWhenMethodIsNotPackageVisible = crashOrWarnWhenMethodIsNotPackageVisible;
  }
}
