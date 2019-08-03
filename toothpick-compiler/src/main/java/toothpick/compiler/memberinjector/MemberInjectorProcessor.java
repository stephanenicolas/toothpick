/*
 * Copyright 2019 Stephane Nicolas
 * Copyright 2019 Daniel Molinero Reguera
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package toothpick.compiler.memberinjector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedOptions;
import javax.inject.Inject;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.ElementFilter;
import toothpick.MemberInjector;
import toothpick.compiler.common.ToothpickProcessor;
import toothpick.compiler.memberinjector.generators.MemberInjectorGenerator;
import toothpick.compiler.memberinjector.targets.FieldInjectionTarget;
import toothpick.compiler.memberinjector.targets.MethodInjectionTarget;

/**
 * This processor's role is to create {@link MemberInjector}. We create factories in different
 * situations :
 *
 * <ul>
 *   <li>When a class {@code Foo} has an {@link javax.inject.Singleton} annotated field : <br>
 *       --> we create a MemberInjector to inject {@code Foo} instances.
 *   <li>When a class {@code Foo} has an {@link javax.inject.Singleton} method : <br>
 *       --> we create a MemberInjector to inject {@code Foo} instances.
 * </ul>
 */
// http://stackoverflow.com/a/2067863/693752
@SupportedAnnotationTypes({ToothpickProcessor.INJECT_ANNOTATION_CLASS_NAME})
@SupportedOptions({
  ToothpickProcessor.PARAMETER_EXCLUDES, //
  ToothpickProcessor.PARAMETER_CRASH_WHEN_INJECTED_METHOD_IS_NOT_PACKAGE
}) //
public class MemberInjectorProcessor extends ToothpickProcessor {

  private Map<TypeElement, List<FieldInjectionTarget>> mapTypeElementToFieldInjectorTargetList;
  private Map<TypeElement, List<MethodInjectionTarget>> mapTypeElementToMethodInjectorTargetList;
  private Map<TypeElement, TypeElement> mapTypeElementToSuperTypeElementThatNeedsInjection;

  private Map<String, TypeElement> allRoundsGeneratedToTypeElement = new HashMap<>();

  @Override
  public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {

    readCommonProcessorOptions();
    readOptionCrashWhenMethodIsNotPackageProtected();

    mapTypeElementToFieldInjectorTargetList = new LinkedHashMap<>();
    mapTypeElementToMethodInjectorTargetList = new LinkedHashMap<>();
    mapTypeElementToSuperTypeElementThatNeedsInjection = new LinkedHashMap<>();
    findAndParseTargets(roundEnv);

    // Generate member scopes
    Set<TypeElement> elementWithInjectionSet = new HashSet<>();
    elementWithInjectionSet.addAll(mapTypeElementToFieldInjectorTargetList.keySet());
    elementWithInjectionSet.addAll(mapTypeElementToMethodInjectorTargetList.keySet());

    for (TypeElement typeElement : elementWithInjectionSet) {
      List<FieldInjectionTarget> fieldInjectionTargetList =
          mapTypeElementToFieldInjectorTargetList.get(typeElement);
      List<MethodInjectionTarget> methodInjectionTargetList =
          mapTypeElementToMethodInjectorTargetList.get(typeElement);
      TypeElement superClassThatNeedsInjection =
          mapTypeElementToSuperTypeElementThatNeedsInjection.get(typeElement);
      MemberInjectorGenerator memberInjectorGenerator =
          new MemberInjectorGenerator(
              typeElement, //
              superClassThatNeedsInjection, //
              fieldInjectionTargetList, //
              methodInjectionTargetList, //
              typeUtils);
      String fileDescription = String.format("MemberInjector for type %s", typeElement);
      writeToFile(memberInjectorGenerator, fileDescription, typeElement);
      allRoundsGeneratedToTypeElement.put(memberInjectorGenerator.getFqcn(), typeElement);
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
    for (VariableElement element :
        ElementFilter.fieldsIn(roundEnv.getElementsAnnotatedWith(Inject.class))) {
      if (!isExcludedByFilters((TypeElement) element.getEnclosingElement())) {
        processInjectAnnotatedField(element, mapTypeElementToFieldInjectorTargetList);
      }
    }
  }

  protected void processInjectAnnotatedMethods(RoundEnvironment roundEnv) {
    for (ExecutableElement element :
        ElementFilter.methodsIn(roundEnv.getElementsAnnotatedWith(Inject.class))) {
      if (!isExcludedByFilters((TypeElement) element.getEnclosingElement())) {
        processInjectAnnotatedMethod(element, mapTypeElementToMethodInjectorTargetList);
      }
    }
  }

  private void processInjectAnnotatedField(
      VariableElement fieldElement,
      Map<TypeElement, List<FieldInjectionTarget>> mapTypeElementToMemberInjectorTargetList) {
    TypeElement enclosingElement = (TypeElement) fieldElement.getEnclosingElement();

    // Verify common generated code restrictions.
    if (!isValidInjectAnnotatedFieldOrParameter(fieldElement)) {
      return;
    }

    List<FieldInjectionTarget> fieldInjectionTargetList =
        mapTypeElementToMemberInjectorTargetList.get(enclosingElement);
    if (fieldInjectionTargetList == null) {
      fieldInjectionTargetList = new ArrayList<>();
      mapTypeElementToMemberInjectorTargetList.put(enclosingElement, fieldInjectionTargetList);
    }

    mapTypeToMostDirectSuperTypeThatNeedsInjection(enclosingElement);
    fieldInjectionTargetList.add(createFieldOrParamInjectionTarget(fieldElement));
  }

  private void processInjectAnnotatedMethod(
      ExecutableElement methodElement,
      Map<TypeElement, List<MethodInjectionTarget>> mapTypeElementToMemberInjectorTargetList) {
    TypeElement enclosingElement = (TypeElement) methodElement.getEnclosingElement();

    // Verify common generated code restrictions.
    if (!isValidInjectAnnotatedMethod(methodElement)) {
      return;
    }

    List<MethodInjectionTarget> methodInjectionTargetList =
        mapTypeElementToMemberInjectorTargetList.get(enclosingElement);
    if (methodInjectionTargetList == null) {
      methodInjectionTargetList = new ArrayList<>();
      mapTypeElementToMemberInjectorTargetList.put(enclosingElement, methodInjectionTargetList);
    }

    mapTypeToMostDirectSuperTypeThatNeedsInjection(enclosingElement);
    methodInjectionTargetList.add(createMethodInjectionTarget(methodElement));
  }

  private void mapTypeToMostDirectSuperTypeThatNeedsInjection(TypeElement typeElement) {
    TypeElement superClassWithInjectedMembers =
        getMostDirectSuperClassWithInjectedMembers(typeElement, true);
    mapTypeElementToSuperTypeElementThatNeedsInjection.put(
        typeElement, superClassWithInjectedMembers);
  }

  private MethodInjectionTarget createMethodInjectionTarget(ExecutableElement methodElement) {
    TypeElement enclosingElement = (TypeElement) methodElement.getEnclosingElement();

    final String methodName = methodElement.getSimpleName().toString();

    boolean isOverride = isOverride(enclosingElement, methodElement);

    MethodInjectionTarget methodInjectionTarget =
        new MethodInjectionTarget(enclosingElement, methodName, isOverride);
    methodInjectionTarget.parameters.addAll(getParamInjectionTargetList(methodElement));
    methodInjectionTarget.exceptionTypes.addAll(getExceptionTypes(methodElement));

    return methodInjectionTarget;
  }

  // used for testing only
  void setToothpickExcludeFilters(String toothpickExcludeFilters) {
    this.toothpickExcludeFilters = toothpickExcludeFilters;
  }

  // used for testing only
  void setCrashOrWarnWhenMethodIsNotPackageVisible(
      boolean crashOrWarnWhenMethodIsNotPackageVisible) {
    this.toothpickCrashWhenMethodIsNotPackageVisible = crashOrWarnWhenMethodIsNotPackageVisible;
  }

  // used for testing only
  TypeElement getOriginatingElement(String generatedQualifiedName) {
    return allRoundsGeneratedToTypeElement.get(generatedQualifiedName);
  }
}
