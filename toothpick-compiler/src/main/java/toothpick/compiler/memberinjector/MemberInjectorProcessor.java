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
    ToothpickProcessor.PARAMETER_REGISTRY_PACKAGE_NAME, //
    ToothpickProcessor.PARAMETER_REGISTRY_CHILDREN_PACKAGE_NAMES, //
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

  private MethodInjectionTarget createMethodInjectionTarget(ExecutableElement methodElement) {
    TypeElement enclosingElement = (TypeElement) methodElement.getEnclosingElement();

    final TypeElement returnType = (TypeElement) typeUtils.asElement(methodElement.getReturnType());
    final String methodName = methodElement.getSimpleName().toString();

    MethodInjectionTarget methodInjectionTarget = new MethodInjectionTarget(enclosingElement, methodName, returnType);
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
}
