package toothpick.compiler.memberinjector;

import java.io.PrintWriter;
import java.io.StringWriter;
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
import toothpick.MemberInjector;
import toothpick.compiler.ToothpickProcessor;
import toothpick.compiler.factory.FactoryProcessor;

import static javax.lang.model.element.Modifier.PRIVATE;
import static toothpick.compiler.ToothpickProcessor.INJECT_ANNOTATION_CLASS_NAME;
import static toothpick.compiler.ToothpickProcessor.PARAMETER_REGISTRY_CHILDREN_PACKAGE_NAMES;
import static toothpick.compiler.ToothpickProcessor.PARAMETER_REGISTRY_PACKAGE_NAME;

/**
 * Same as {@link FactoryProcessor} but for {@link MemberInjector} classes.
 * Annotation processor arguments are shared with the {@link FactoryProcessor}.
 *
 * @see FactoryProcessor
 */
//TODO add a @Generated annotation on generated classes, the value is the name of the factory class
@SupportedAnnotationTypes({ INJECT_ANNOTATION_CLASS_NAME })
@SupportedOptions({ PARAMETER_REGISTRY_PACKAGE_NAME+"."+PARAMETER_REGISTRY_CHILDREN_PACKAGE_NAMES }) //
public class MemberInjectorProcessor extends ToothpickProcessor {

  private Map<TypeElement, List<MemberInjectorInjectionTarget>> targetClassMap = new LinkedHashMap<>();

  @Override public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
    findAndParseTargets(roundEnv);

    if (!roundEnv.processingOver()) {
      return false;
    }

    // Generate member injectors
    for (Map.Entry<TypeElement, List<MemberInjectorInjectionTarget>> entry : targetClassMap.entrySet()) {
      List<MemberInjectorInjectionTarget> memberInjectorInjectionTargetList = entry.getValue();
      MemberInjectorGenerator memberInjectorGenerator = new MemberInjectorGenerator(memberInjectorInjectionTargetList);
      TypeElement typeElement = entry.getKey();
      String fileDescription = String.format("MemberInjector for type %s", typeElement);
      writeToFile(memberInjectorGenerator, fileDescription, typeElement);
    }

    // Generate Registry
    if (readParameters()) {
      MemberInjectorRegistryInjectionTarget memberInjectorRegistryInjectionTarget =
          new MemberInjectorRegistryInjectionTarget(targetClassMap.keySet(), toothpickRegistryPackageName, toothpickRegistryChildrenPackageNameList);
      MemberInjectorRegistryGenerator memberInjectorRegistryGenerator = new MemberInjectorRegistryGenerator(memberInjectorRegistryInjectionTarget);
      Element[] allTypes = targetClassMap.keySet().toArray(new Element[targetClassMap.size()]);
      String fileDescription = "MemberInjector registry";
      writeToFile(memberInjectorRegistryGenerator, fileDescription, allTypes);
    }

    return false;
  }

  private void findAndParseTargets(RoundEnvironment roundEnv) {
    for (Element element : roundEnv.getElementsAnnotatedWith(Inject.class)) {
      if (element.getKind() == ElementKind.FIELD) {
        try {
          parseInject(element, targetClassMap);
        } catch (Exception e) {
          StringWriter stackTrace = new StringWriter();
          e.printStackTrace(new PrintWriter(stackTrace));

          error(element, "Unable to generate MemberInjector when parsing @Inject.\n\n%s", stackTrace.toString());
        }
      }
    }
  }

  private void parseInject(Element element, Map<TypeElement, List<MemberInjectorInjectionTarget>> targetClassMap) {
    TypeElement enclosingElement = (TypeElement) element.getEnclosingElement();

    // Verify common generated code restrictions.
    if (!isValidInjectField(element)) {
      return;
    }

    List<MemberInjectorInjectionTarget> memberInjectorInjectionTargetList = targetClassMap.get(enclosingElement);
    if (memberInjectorInjectionTargetList == null) {
      memberInjectorInjectionTargetList = new ArrayList<>();
      targetClassMap.put(enclosingElement, memberInjectorInjectionTargetList);
    }
    memberInjectorInjectionTargetList.add(createInjectionTarget(element));
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

  private MemberInjectorInjectionTarget createInjectionTarget(Element element) {
    TypeElement enclosingElement = (TypeElement) element.getEnclosingElement();

    final String targetClassPackage = getPackageName(enclosingElement);
    final String targetClassName = getClassName(enclosingElement, targetClassPackage);
    final String targetClass = enclosingElement.getQualifiedName().toString();
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

    MemberInjectorInjectionTarget.Kind kind = getKind(element);
    final String kindParamPackageName;
    final String kindParamClassName;
    if (kind == MemberInjectorInjectionTarget.Kind.INSTANCE) {
      kindParamPackageName = null;
      kindParamClassName = null;
    } else {
      TypeElement kindParameterTypeElement = getKindParameter(element);
      kindParamPackageName = getPackageName(kindParameterTypeElement);
      kindParamClassName = getClassName(kindParameterTypeElement, kindParamPackageName);
    }
    return new MemberInjectorInjectionTarget(targetClassPackage, targetClassName, targetClass, memberClassPackage, memberClassName, memberName,
        superClassThatNeedsInjectionClassPackage, superClassThatNeedsInjectionClassName, kind, kindParamPackageName, kindParamClassName);
  }

  private MemberInjectorInjectionTarget.Kind getKind(Element element) {
    TypeMirror elementTypeMirror = element.asType();
    String elementTypeName = typeUtils.erasure(elementTypeMirror).toString();
    if ("javax.inject.Provider".equals(elementTypeName)) {
      return MemberInjectorInjectionTarget.Kind.PROVIDER;
    } else if ("toothpick.Lazy".equals(elementTypeName)) {
      return MemberInjectorInjectionTarget.Kind.LAZY;
    } else if ("java.util.concurrent.Future".equals(elementTypeName)) {
      return MemberInjectorInjectionTarget.Kind.FUTURE;
    } else {
      return MemberInjectorInjectionTarget.Kind.INSTANCE;
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
}
