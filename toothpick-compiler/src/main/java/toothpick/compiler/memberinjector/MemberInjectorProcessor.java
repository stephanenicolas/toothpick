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
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import toothpick.MemberInjector;
import toothpick.compiler.ToothpickProcessor;
import toothpick.compiler.factory.FactoryProcessor;
import toothpick.compiler.memberinjector.generators.MemberInjectorGenerator;
import toothpick.compiler.memberinjector.generators.MemberInjectorRegistryGenerator;
import toothpick.compiler.memberinjector.targets.FieldInjectionTarget;
import toothpick.compiler.memberinjector.targets.MemberInjectorRegistryInjectionTarget;
import toothpick.compiler.memberinjector.targets.MethodInjectionTarget;

/**
 * Same as {@link FactoryProcessor} but for {@link MemberInjector} classes.
 * Annotation processor arguments are shared with the {@link FactoryProcessor}.
 *
 * @see FactoryProcessor
 */
//http://stackoverflow.com/a/2067863/693752
@SupportedAnnotationTypes({ ToothpickProcessor.INJECT_ANNOTATION_CLASS_NAME })
@SupportedOptions({ ToothpickProcessor.PARAMETER_REGISTRY_PACKAGE_NAME + "." + ToothpickProcessor.PARAMETER_REGISTRY_CHILDREN_PACKAGE_NAMES }) //
public class MemberInjectorProcessor extends ToothpickProcessor {

  private Map<TypeElement, List<FieldInjectionTarget>> mapTypeElementToFieldInjectorTargetList = new LinkedHashMap<>();
  private Map<TypeElement, List<MethodInjectionTarget>> mapTypeElementToMethodInjectorTargetList = new LinkedHashMap<>();
  private Map<TypeElement, TypeElement> mapTypeElementToSuperTypeElementThatNeedsInjection = new LinkedHashMap<>();

  @Override public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
    findAndParseTargets(roundEnv);

    if (!roundEnv.processingOver()) {
      return false;
    }

    // Generate member injectors
    Set<TypeElement> elementWithInjectionSet = new HashSet<>();
    elementWithInjectionSet.addAll(mapTypeElementToFieldInjectorTargetList.keySet());
    elementWithInjectionSet.addAll(mapTypeElementToMethodInjectorTargetList.keySet());
    for (TypeElement typeElement : elementWithInjectionSet) {
      List<FieldInjectionTarget> fieldInjectionTargetList = mapTypeElementToFieldInjectorTargetList.get(typeElement);
      List<MethodInjectionTarget> methodInjectionTargetList = mapTypeElementToMethodInjectorTargetList.get(typeElement);
      TypeElement superClassThatNeedsInjection = mapTypeElementToSuperTypeElementThatNeedsInjection.get(typeElement);
      MemberInjectorGenerator memberInjectorGenerator =
          new MemberInjectorGenerator(typeElement, superClassThatNeedsInjection, fieldInjectionTargetList, methodInjectionTargetList);
      String fileDescription = String.format("MemberInjector for type %s", typeElement);
      writeToFile(memberInjectorGenerator, fileDescription, typeElement);
    }

    // Generate Registry
    if (toothpickRegistryPackageName != null || readProcessorOptions()) {
      MemberInjectorRegistryInjectionTarget memberInjectorRegistryInjectionTarget =
          new MemberInjectorRegistryInjectionTarget(elementWithInjectionSet, toothpickRegistryPackageName, toothpickRegistryChildrenPackageNameList);
      MemberInjectorRegistryGenerator memberInjectorRegistryGenerator = new MemberInjectorRegistryGenerator(memberInjectorRegistryInjectionTarget);
      Element[] allTypes = elementWithInjectionSet.toArray(new Element[mapTypeElementToFieldInjectorTargetList.size()]);
      String fileDescription = "MemberInjector registry";
      writeToFile(memberInjectorRegistryGenerator, fileDescription, allTypes);
    }

    return false;
  }

  private void findAndParseTargets(RoundEnvironment roundEnv) {
    parseInjectedFields(roundEnv);
    parseInjectedMethods(roundEnv);
  }

  protected void parseInjectedFields(RoundEnvironment roundEnv) {
    for (VariableElement element : ElementFilter.fieldsIn(roundEnv.getElementsAnnotatedWith(Inject.class))) {
      parseInjectedField(element, mapTypeElementToFieldInjectorTargetList);
    }
  }

  protected void parseInjectedMethods(RoundEnvironment roundEnv) {
    for (ExecutableElement element : ElementFilter.methodsIn(roundEnv.getElementsAnnotatedWith(Inject.class))) {
      parseInjectedMethod(element, mapTypeElementToMethodInjectorTargetList);
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
  //it will be performed by the super class member injector already.
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
    TypeElement superClassWithInjectedMembers = getMostDirectSuperClassWithInjectedMembers(enclosingElement);
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
    return new FieldInjectionTarget(memberTypeElement, memberName, kind, kindParameterTypeElement);
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
    } else if ("java.util.concurrent.Future".equals(elementTypeName)) {
      return FieldInjectionTarget.Kind.FUTURE;
    } else {
      return FieldInjectionTarget.Kind.INSTANCE;
    }
  }

  private TypeElement getKindParameter(Element element) {
    TypeMirror elementTypeMirror = element.asType();
    TypeMirror firstParameterTypeMirror = ((DeclaredType) elementTypeMirror).getTypeArguments().get(0);
    return (TypeElement) typeUtils.asElement(firstParameterTypeMirror);
  }

  private TypeElement getMostDirectSuperClassWithInjectedMembers(TypeElement typeElement) {
    TypeElement currentTypeElement = typeElement;
    boolean couldFindSuperClass = true;
    do {
      TypeMirror superClassTypeMirror = currentTypeElement.getSuperclass();
      couldFindSuperClass = superClassTypeMirror.getKind() == TypeKind.DECLARED;
      if (couldFindSuperClass) {
        currentTypeElement = (TypeElement) ((DeclaredType) superClassTypeMirror).asElement();
        for (Element enclosedElement : currentTypeElement.getEnclosedElements()) {
          if ((enclosedElement.getKind() == ElementKind.FIELD || enclosedElement.getKind() == ElementKind.METHOD)
              && enclosedElement.getAnnotation(Inject.class) != null) {
            return currentTypeElement;
          }
        }
      }
    } while (couldFindSuperClass);
    return null;
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
