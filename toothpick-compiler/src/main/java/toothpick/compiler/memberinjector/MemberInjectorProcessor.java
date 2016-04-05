package toothpick.compiler.memberinjector;

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
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import toothpick.MemberInjector;
import toothpick.compiler.ToothpickProcessor;
import toothpick.compiler.factory.FactoryProcessor;

import static javax.lang.model.element.Modifier.PRIVATE;

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

  private Map<TypeElement, List<MemberInjectorFieldInjectionTarget>> mapTypeElementToMemberInjectorTargetList = new LinkedHashMap<>();

  @Override public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
    findAndParseTargets(roundEnv);

    if (!roundEnv.processingOver()) {
      return false;
    }

    // Generate member injectors
    for (Map.Entry<TypeElement, List<MemberInjectorFieldInjectionTarget>> entry : mapTypeElementToMemberInjectorTargetList.entrySet()) {
      List<MemberInjectorFieldInjectionTarget> memberInjectorFieldInjectionTargetList = entry.getValue();
      MemberInjectorGenerator memberInjectorGenerator = new MemberInjectorGenerator(memberInjectorFieldInjectionTargetList);
      TypeElement typeElement = entry.getKey();
      String fileDescription = String.format("MemberInjector for type %s", typeElement);
      writeToFile(memberInjectorGenerator, fileDescription, typeElement);
    }

    // Generate Registry
    if (readParameters()) {
      MemberInjectorRegistryInjectionTarget memberInjectorRegistryInjectionTarget =
          new MemberInjectorRegistryInjectionTarget(mapTypeElementToMemberInjectorTargetList.keySet(), toothpickRegistryPackageName,
              toothpickRegistryChildrenPackageNameList);
      MemberInjectorRegistryGenerator memberInjectorRegistryGenerator = new MemberInjectorRegistryGenerator(memberInjectorRegistryInjectionTarget);
      Element[] allTypes = mapTypeElementToMemberInjectorTargetList.keySet().toArray(new Element[mapTypeElementToMemberInjectorTargetList.size()]);
      String fileDescription = "MemberInjector registry";
      writeToFile(memberInjectorRegistryGenerator, fileDescription, allTypes);
    }

    return false;
  }

  private void findAndParseTargets(RoundEnvironment roundEnv) {
    //TODO support method injection
    for (Element element : ElementFilter.fieldsIn(roundEnv.getElementsAnnotatedWith(Inject.class))) {
      parseInjectedField(element, mapTypeElementToMemberInjectorTargetList);
    }
  }

  private void parseInjectedField(Element element, Map<TypeElement, List<MemberInjectorFieldInjectionTarget>> targetClassMap) {
    TypeElement enclosingElement = (TypeElement) element.getEnclosingElement();

    // Verify common generated code restrictions.
    if (!isValidInjectField(element)) {
      return;
    }

    List<MemberInjectorFieldInjectionTarget> memberInjectorFieldInjectionTargetList = targetClassMap.get(enclosingElement);
    if (memberInjectorFieldInjectionTargetList == null) {
      memberInjectorFieldInjectionTargetList = new ArrayList<>();
      targetClassMap.put(enclosingElement, memberInjectorFieldInjectionTargetList);
    }
    memberInjectorFieldInjectionTargetList.add(createInjectionTargetFromField(element));
  }

  private boolean isValidInjectField(Element element) {
    boolean valid = true;
    TypeElement enclosingElement = (TypeElement) element.getEnclosingElement();

    // Verify modifiers.
    Set<Modifier> modifiers = element.getModifiers();
    if (modifiers.contains(PRIVATE)) {
      error(element, "@%s fields must not be private. (%s)", Inject.class.getName(), enclosingElement.getQualifiedName());
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

  private MemberInjectorFieldInjectionTarget createInjectionTargetFromField(Element element) {
    TypeElement enclosingElement = (TypeElement) element.getEnclosingElement();

    final String targetClassPackage = getPackageName(enclosingElement);
    final String targetClassName = getClassName(enclosingElement, targetClassPackage);
    final TypeElement memberTypeElement = (TypeElement) typeUtils.asElement(element.asType());
    final String memberClassPackage = getPackageName(memberTypeElement);
    final String memberClassName = getClassName(memberTypeElement, memberClassPackage);
    final String memberName = element.getSimpleName().toString();
    final TypeElement superTypeElementWithInjectedFields = getSuperClassWithInjectedFields(enclosingElement);
    final String superClassThatNeedsInjectionClassPackage;
    final String superClassThatNeedsInjectionClassName;
    if (superTypeElementWithInjectedFields == null) {
      superClassThatNeedsInjectionClassPackage = null;
      superClassThatNeedsInjectionClassName = null;
    } else {
      superClassThatNeedsInjectionClassPackage = getPackageName(superTypeElementWithInjectedFields);
      superClassThatNeedsInjectionClassName = getClassName(superTypeElementWithInjectedFields, superClassThatNeedsInjectionClassPackage);
    }

    MemberInjectorFieldInjectionTarget.Kind kind = getKind(element);
    final String kindParamPackageName;
    final String kindParamClassName;
    if (kind == MemberInjectorFieldInjectionTarget.Kind.INSTANCE) {
      kindParamPackageName = null;
      kindParamClassName = null;
    } else {
      TypeElement kindParameterTypeElement = getKindParameter(element);
      kindParamPackageName = getPackageName(kindParameterTypeElement);
      kindParamClassName = getClassName(kindParameterTypeElement, kindParamPackageName);
    }
    return new MemberInjectorFieldInjectionTarget(targetClassPackage, targetClassName, memberClassPackage, memberClassName, memberName,
        superClassThatNeedsInjectionClassPackage, superClassThatNeedsInjectionClassName, kind, kindParamPackageName, kindParamClassName);
  }

  private MemberInjectorFieldInjectionTarget.Kind getKind(Element element) {
    TypeMirror elementTypeMirror = element.asType();
    String elementTypeName = typeUtils.erasure(elementTypeMirror).toString();
    if ("javax.inject.Provider".equals(elementTypeName)) {
      return MemberInjectorFieldInjectionTarget.Kind.PROVIDER;
    } else if ("toothpick.Lazy".equals(elementTypeName)) {
      return MemberInjectorFieldInjectionTarget.Kind.LAZY;
    } else if ("java.util.concurrent.Future".equals(elementTypeName)) {
      return MemberInjectorFieldInjectionTarget.Kind.FUTURE;
    } else {
      return MemberInjectorFieldInjectionTarget.Kind.INSTANCE;
    }
  }

  private TypeElement getKindParameter(Element element) {
    TypeMirror elementTypeMirror = element.asType();
    TypeMirror firstParameterTypeMirror = ((DeclaredType) elementTypeMirror).getTypeArguments().get(0);
    return (TypeElement) typeUtils.asElement(firstParameterTypeMirror);
  }

  private TypeElement getSuperClassWithInjectedFields(TypeElement typeElement) {
    TypeElement currentTypeElement = typeElement;
    boolean failedToFindSuperClass = false;
    do {
      TypeMirror superClassTypeMirror = currentTypeElement.getSuperclass();
      failedToFindSuperClass = superClassTypeMirror.getKind() != TypeKind.DECLARED;
      if (!failedToFindSuperClass) {
        currentTypeElement = (TypeElement) ((DeclaredType) superClassTypeMirror).asElement();
        for (Element enclosedElement : currentTypeElement.getEnclosedElements()) {
          if (enclosedElement.getKind() == ElementKind.FIELD && enclosedElement.getAnnotation(Inject.class) != null) {
            return currentTypeElement;
          }
        }
      }
    } while (!failedToFindSuperClass && !"java.lang.Object".equals(currentTypeElement.getQualifiedName()));
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
