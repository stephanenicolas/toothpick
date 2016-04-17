package toothpick.compiler.common;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;
import javax.inject.Inject;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.JavaFileObject;
import toothpick.compiler.common.generators.CodeGenerator;
import toothpick.compiler.common.generators.targets.ParamInjectionTarget;
import toothpick.compiler.memberinjector.targets.FieldInjectionTarget;

import static javax.lang.model.element.Modifier.PRIVATE;
import static javax.tools.Diagnostic.Kind.ERROR;
import static javax.tools.Diagnostic.Kind.WARNING;

/**
 * Base processor class.
 */
public abstract class ToothpickProcessor extends AbstractProcessor {

  /** The name of the {@link javax.inject.Inject} annotation class that triggers {@code ToothpickProcessor}s. */
  public static final String INJECT_ANNOTATION_CLASS_NAME = "javax.inject.Inject";

  /**
   * The name of the annotation processor option to declare in which package a registry should be generated.
   * If this parameter is not passed, no registry is generated.
   */
  public static final String PARAMETER_REGISTRY_PACKAGE_NAME = "toothpick_registry_package_name";

  /**
   * The name of the annotation processor option to exclude classes from the creation of member scopes & factories.
   */
  public static final String PARAMETER_EXCLUDES = "toothpick_excludes";

  /**
   * The name annotation processor option to declare in which packages reside the registries used by the generated registry, if it is created.
   *
   * @see #PARAMETER_REGISTRY_PACKAGE_NAME
   */
  public static final String PARAMETER_REGISTRY_CHILDREN_PACKAGE_NAMES = "toothpick_registry_children_package_names";

  protected Elements elementUtils;
  protected Types typeUtils;
  protected Filer filer;

  protected String toothpickRegistryPackageName;
  protected List<String> toothpickRegistryChildrenPackageNameList;
  protected String toothpickExcludeFilters = "java,android";

  @Override
  public synchronized void init(ProcessingEnvironment processingEnv) {
    super.init(processingEnv);

    elementUtils = processingEnv.getElementUtils();
    typeUtils = processingEnv.getTypeUtils();
    filer = processingEnv.getFiler();
  }

  protected boolean writeToFile(CodeGenerator codeGenerator, String fileDescription, Element... originatingElements) {
    Writer writer = null;
    boolean success = true;

    try {
      JavaFileObject jfo = filer.createSourceFile(codeGenerator.getFqcn(), originatingElements);
      writer = jfo.openWriter();
      writer.write(codeGenerator.brewJava());
    } catch (IOException e) {
      error("Error writing %s file: %s", fileDescription, e.getMessage());
      success = false;
    } finally {
      if (writer != null) {
        try {
          writer.close();
        } catch (IOException e) {
          error("Error closing %s file: %s", fileDescription, e.getMessage());
          success = false;
        }
      }
    }

    return success;
  }

  /**
   * Reads both annotation compilers {@link ToothpickProcessor#PARAMETER_REGISTRY_PACKAGE_NAME} and
   * {@link ToothpickProcessor#PARAMETER_REGISTRY_CHILDREN_PACKAGE_NAMES} options from the arguments
   * passed to the processor.
   *
   * @return true if toothpickRegistryPackageName is defined, false otherwise.
   */
  protected boolean readProcessorOptions() {
    toothpickRegistryPackageName = processingEnv.getOptions().get(PARAMETER_REGISTRY_PACKAGE_NAME);
    if (toothpickRegistryPackageName == null) {
      warning("No option -Atoothpick_registry_package_name was passed to the compiler."
          + " No registries are generated. Will fallback on reflection at runtime to find factories.");
      return false;
    }

    String toothpickRegistryChildrenPackageNames = processingEnv.getOptions().get(PARAMETER_REGISTRY_CHILDREN_PACKAGE_NAMES);
    toothpickRegistryChildrenPackageNameList = new ArrayList<>();
    if (toothpickRegistryChildrenPackageNames != null) {
      String[] registryPackageNames = toothpickRegistryChildrenPackageNames.split(":");
      for (String registryPackageName : registryPackageNames) {
        toothpickRegistryChildrenPackageNameList.add(registryPackageName.trim());
      }
    }

    toothpickExcludeFilters = processingEnv.getOptions().get(PARAMETER_EXCLUDES);

    return true;
  }

  protected String getPackageName(TypeElement type) {
    return elementUtils.getPackageOf(type).getQualifiedName().toString();
  }

  protected String getClassName(TypeElement type, String packageName) {
    int packageLen = packageName.length() + 1;
    return type.getQualifiedName().toString().substring(packageLen).replace('.', '$');
  }

  /**
   * Returns {@code true} if the an annotation is found on the given element with the given class
   * name (not fully qualified).
   */
  protected boolean hasAnnotationWithName(Element element, String simpleName) {
    for (AnnotationMirror mirror : element.getAnnotationMirrors()) {
      final Element annnotationElement = mirror.getAnnotationType().asElement();
      String annotationName = annnotationElement.getSimpleName().toString();
      if (simpleName.equals(annotationName)) {
        return true;
      }
    }
    return false;
  }

  protected void error(String message, Object... args) {
    processingEnv.getMessager().printMessage(ERROR, String.format(message, args));
  }

  protected void error(Element element, String message, Object... args) {
    processingEnv.getMessager().printMessage(ERROR, String.format(message, args), element);
  }

  protected void warning(String message, Object... args) {
    processingEnv.getMessager().printMessage(WARNING, String.format(message, args));
  }

  protected boolean isValidInjectField(VariableElement fieldElement) {
    boolean valid = true;
    TypeElement enclosingElement = (TypeElement) fieldElement.getEnclosingElement();

    Element rawFieldTypeElement = typeUtils.asElement(fieldElement.asType());

    if (!(rawFieldTypeElement instanceof TypeElement)) {
      error(fieldElement, "Field %s#%s is of type %s which is not supported by Toothpick.", enclosingElement.getQualifiedName(),
          fieldElement.getSimpleName(), rawFieldTypeElement);
      return false;
    }

    // Verify modifiers.
    Set<Modifier> modifiers = fieldElement.getModifiers();
    if (modifiers.contains(PRIVATE)) {
      error(fieldElement, "@Inject annotated fields must be non private : %s#%s", enclosingElement.getQualifiedName(), fieldElement.getSimpleName());
      valid = false;
    }

    // Verify parentScope modifiers.
    Set<Modifier> parentModifiers = enclosingElement.getModifiers();
    //TODO should not be a non static inner class neither
    if (parentModifiers.contains(PRIVATE)) {
      error(fieldElement, "@Injected fields in class %s. The class must be non private.", enclosingElement.getSimpleName());
      valid = false;
    }

    return valid;
  }

  protected boolean isValidInjectMethod(Element methodElement) {
    boolean valid = true;
    TypeElement enclosingElement = (TypeElement) methodElement.getEnclosingElement();

    // Verify modifiers.
    Set<Modifier> modifiers = methodElement.getModifiers();
    if (modifiers.contains(PRIVATE)) {
      error(methodElement, "@Inject annotated methods must not be private : %s#%s", enclosingElement.getQualifiedName(),
          methodElement.getSimpleName());
      valid = false;
    }

    // Verify parentScope modifiers.
    Set<Modifier> parentModifiers = enclosingElement.getModifiers();
    //TODO should not be a non static inner class neither
    if (parentModifiers.contains(PRIVATE)) {
      error(methodElement, "@Injected fields in class %s. The class must be non private.", enclosingElement.getSimpleName());
      valid = false;
    }

    return valid;
  }

  protected List<ParamInjectionTarget> getParamInjectionTargetList(ExecutableElement executableElement) {
    List<ParamInjectionTarget> paramInjectionTarget = new ArrayList<>();
    for (VariableElement variableElement : executableElement.getParameters()) {
      paramInjectionTarget.add(createFieldInjectionTarget(variableElement));
    }
    return paramInjectionTarget;
  }

  protected boolean isExcludedByFilters(TypeElement fieldTypeElement) {
    String typeElementName = fieldTypeElement.getQualifiedName().toString();
    for (String exclude : toothpickExcludeFilters.split(",")) {
      if (typeElementName.startsWith(exclude.trim())) {
        return true;
      }
    }
    return false;
  }

  public TypeElement getMostDirectSuperClassWithInjectedMembers(TypeElement typeElement, boolean onlyParents) {
    TypeElement currentTypeElement = typeElement;
    do {
      if (currentTypeElement != typeElement || !onlyParents) {
        List<? extends Element> enclosedElements = currentTypeElement.getEnclosedElements();
        for (Element enclosedElement : enclosedElements) {
          if ((enclosedElement.getAnnotation(Inject.class) != null && enclosedElement.getKind() == ElementKind.FIELD)
              || (enclosedElement.getAnnotation(Inject.class) != null && enclosedElement.getKind() == ElementKind.METHOD)) {
            return currentTypeElement;
          }
        }
      }
      TypeMirror superclass = currentTypeElement.getSuperclass();
      if (superclass.getKind() == TypeKind.DECLARED) {
        DeclaredType superType = (DeclaredType) superclass;
        currentTypeElement = (TypeElement) superType.asElement();
      } else {
        currentTypeElement = null;
      }
    } while (currentTypeElement != null);
    return null;
  }

  protected FieldInjectionTarget createFieldInjectionTarget(VariableElement element) {
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

  //http://stackoverflow.com/a/36678792/693752
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
}
