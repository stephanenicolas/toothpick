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
package toothpick.compiler.common;

import static java.lang.String.format;
import static javax.lang.model.element.Modifier.PRIVATE;
import static javax.lang.model.element.Modifier.PROTECTED;
import static javax.lang.model.element.Modifier.PUBLIC;
import static javax.tools.Diagnostic.Kind.ERROR;
import static javax.tools.Diagnostic.Kind.WARNING;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;
import javax.inject.Inject;
import javax.lang.model.SourceVersion;
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

/** Base processor class. */
public abstract class ToothpickProcessor extends AbstractProcessor {

  /**
   * The name of the {@link javax.inject.Inject} annotation class that triggers {@code
   * ToothpickProcessor}s.
   */
  public static final String INJECT_ANNOTATION_CLASS_NAME = "javax.inject.Inject";

  public static final String SINGLETON_ANNOTATION_CLASS_NAME = "javax.inject.Singleton";
  public static final String PRODUCES_SINGLETON_ANNOTATION_CLASS_NAME =
      "toothpick.ProvidesSingleton";
  public static final String INJECT_CONSTRUCTOR_ANNOTATION_CLASS_NAME =
      "toothpick.InjectConstructor";

  /**
   * The name of the annotation processor option to exclude classes from the creation of member
   * scopes & factories. Exclude filters are java regex, multiple entries are comma separated.
   */
  public static final String PARAMETER_EXCLUDES = "toothpick_excludes";

  /**
   * The name of the annotation processor option to let TP know about custom scope annotation
   * classes. This option is needed only in the case where a custom scope annotation is used on a
   * class, and this class doesn't use any annotation processed out of the box by TP (i.e.
   * javax.inject.* annotations). If you use custom scope annotations, it is a good practice to
   * always use this option so that developers can use the new scope annotation in a very free way
   * without having to consider the annotation processing internals.
   */
  public static final String PARAMETER_ANNOTATION_TYPES = "toothpick_annotations";

  /**
   * The name of the annotation processor option to make the TP annotation processor crash when it
   * can't generate a factory for a class. By default the behavior is not to crash but emit a
   * warning. Passing the value {@code true} crashes the build instead.
   */
  public static final String PARAMETER_CRASH_WHEN_NO_FACTORY_CAN_BE_CREATED =
      "toothpick_crash_when_no_factory_can_be_created";

  /**
   * The name of the annotation processor option to make the TP annotation processor crash when it
   * detects an annotated method but with a non package-private visibility. By default the behavior
   * is not to crash but emit a warning. Passing the value {@code true} crashes the build instead.
   */
  public static final String PARAMETER_CRASH_WHEN_INJECTED_METHOD_IS_NOT_PACKAGE =
      "toothpick_crash_when_injected_method_is_not_package";

  /** Allows to suppress warning when an injected method is not package-private visible. */
  private static final String SUPPRESS_WARNING_ANNOTATION_VISIBLE_VALUE = "visible";

  protected Elements elementUtils;
  protected Types typeUtils;
  protected Filer filer;

  protected String toothpickExcludeFilters = "java.*,android.*";
  protected Boolean toothpickCrashWhenMethodIsNotPackageVisible;
  protected Set<String> supportedAnnotationTypes = new HashSet<>();

  @Override
  public synchronized void init(ProcessingEnvironment processingEnv) {
    super.init(processingEnv);

    elementUtils = processingEnv.getElementUtils();
    typeUtils = processingEnv.getTypeUtils();
    filer = processingEnv.getFiler();
  }

  @Override
  public SourceVersion getSupportedSourceVersion() {
    return SourceVersion.latestSupported();
  }

  public void addSupportedAnnotationType(String typeFQN) {
    supportedAnnotationTypes.add(typeFQN);
  }

  protected boolean writeToFile(
      CodeGenerator codeGenerator, String fileDescription, Element originatingElement) {
    Writer writer = null;
    boolean success = true;

    try {
      JavaFileObject jfo = filer.createSourceFile(codeGenerator.getFqcn(), originatingElement);
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
   * Reads both annotation compilers {@link ToothpickProcessor#PARAMETER_EXCLUDES} option from the
   * arguments passed to the processor.
   */
  protected void readCommonProcessorOptions() {
    readOptionExcludes();
  }

  private void readOptionExcludes() {
    Map<String, String> options = processingEnv.getOptions();
    if (options.containsKey(PARAMETER_EXCLUDES)) {
      toothpickExcludeFilters = options.get(PARAMETER_EXCLUDES);
    }
  }

  protected void readOptionAnnotationTypes() {
    Map<String, String> options = processingEnv.getOptions();
    if (options.containsKey(PARAMETER_ANNOTATION_TYPES)) {
      String additionalAnnotationTypes = options.get(PARAMETER_ANNOTATION_TYPES);
      for (String additionalAnnotationType : additionalAnnotationTypes.split(",")) {
        supportedAnnotationTypes.add(additionalAnnotationType.trim());
      }
    }
  }

  protected void error(String message, Object... args) {
    processingEnv.getMessager().printMessage(ERROR, format(message, args));
  }

  protected void error(Element element, String message, Object... args) {
    processingEnv.getMessager().printMessage(ERROR, format(message, args), element);
  }

  protected void warning(Element element, String message, Object... args) {
    processingEnv.getMessager().printMessage(WARNING, format(message, args), element);
  }

  protected void warning(String message, Object... args) {
    processingEnv.getMessager().printMessage(WARNING, format(message, args));
  }

  private void crashOrWarnWhenMethodIsNotPackageVisible(Element element, String message) {
    if (toothpickCrashWhenMethodIsNotPackageVisible != null
        && toothpickCrashWhenMethodIsNotPackageVisible) {
      error(element, message);
    } else {
      warning(element, message);
    }
  }

  protected boolean isValidInjectAnnotatedFieldOrParameter(VariableElement variableElement) {
    TypeElement enclosingElement = (TypeElement) variableElement.getEnclosingElement();

    // Verify modifiers.
    Set<Modifier> modifiers = variableElement.getModifiers();
    if (modifiers.contains(PRIVATE)) {
      error(
          variableElement,
          "@Inject annotated fields must be non private : %s#%s",
          enclosingElement.getQualifiedName(),
          variableElement.getSimpleName());
      return false;
    }

    // Verify parentScope modifiers.
    Set<Modifier> parentModifiers = enclosingElement.getModifiers();
    if (parentModifiers.contains(PRIVATE)) {
      error(
          variableElement,
          "@Injected fields in class %s. The class must be non private.",
          enclosingElement.getSimpleName());
      return false;
    }

    if (!isValidInjectedType(variableElement)) {
      return false;
    }
    return true;
  }

  protected boolean isValidInjectAnnotatedMethod(ExecutableElement methodElement) {
    TypeElement enclosingElement = (TypeElement) methodElement.getEnclosingElement();

    // Verify modifiers.
    Set<Modifier> modifiers = methodElement.getModifiers();
    if (modifiers.contains(PRIVATE)) {
      error(
          methodElement,
          "@Inject annotated methods must not be private : %s#%s",
          enclosingElement.getQualifiedName(),
          methodElement.getSimpleName());
      return false;
    }

    // Verify parentScope modifiers.
    Set<Modifier> parentModifiers = enclosingElement.getModifiers();
    if (parentModifiers.contains(PRIVATE)) {
      error(
          methodElement,
          "@Injected fields in class %s. The class must be non private.",
          enclosingElement.getSimpleName());
      return false;
    }

    for (VariableElement paramElement : methodElement.getParameters()) {
      if (!isValidInjectedType(paramElement)) {
        return false;
      }
    }

    if (modifiers.contains(PUBLIC) || modifiers.contains(PROTECTED)) {
      if (!hasWarningSuppressed(methodElement, SUPPRESS_WARNING_ANNOTATION_VISIBLE_VALUE)) {
        crashOrWarnWhenMethodIsNotPackageVisible(
            methodElement,
            format(
                "@Inject annotated methods should have package visibility: %s#%s", //
                enclosingElement.getQualifiedName(), methodElement.getSimpleName()));
      }
    }
    return true;
  }

  protected boolean isValidInjectedType(VariableElement injectedTypeElement) {
    if (!isValidInjectedElementKind(injectedTypeElement)) {
      return false;
    }
    if (isProviderOrLazy(injectedTypeElement) && !isValidProviderOrLazy(injectedTypeElement)) {
      return false;
    }
    return true;
  }

  private boolean isValidInjectedElementKind(VariableElement injectedTypeElement) {
    Element typeElement = typeUtils.asElement(injectedTypeElement.asType());
    // typeElement can be null for primitives.
    // https://github.com/stephanenicolas/toothpick/issues/261
    if (typeElement == null //
        || typeElement.getKind() != ElementKind.CLASS //
            && typeElement.getKind() != ElementKind.INTERFACE //
            && typeElement.getKind() != ElementKind.ENUM) {

      // find the class containing the element
      // the element can be a field or a parameter
      Element enclosingElement = injectedTypeElement.getEnclosingElement();
      final String typeName;
      if (typeElement != null) {
        typeName = typeElement.toString();
      } else {
        typeName = injectedTypeElement.asType().toString();
      }
      if (enclosingElement instanceof TypeElement) {
        error(
            injectedTypeElement,
            "Field %s#%s is of type %s which is not supported by Toothpick.",
            ((TypeElement) enclosingElement).getQualifiedName(),
            injectedTypeElement.getSimpleName(),
            typeName);
      } else {
        Element methodOrConstructorElement = enclosingElement;
        enclosingElement = enclosingElement.getEnclosingElement();
        error(
            injectedTypeElement,
            "Parameter %s in method/constructor %s#%s is of type %s which is not supported by Toothpick.",
            injectedTypeElement.getSimpleName(), //
            ((TypeElement) enclosingElement).getQualifiedName(), //
            methodOrConstructorElement.getSimpleName(), //
            typeName);
      }
      return false;
    }
    return true;
  }

  private boolean isValidProviderOrLazy(Element element) {
    DeclaredType declaredType = (DeclaredType) element.asType();

    // Contains type parameter
    if (declaredType.getTypeArguments().isEmpty()) {
      Element enclosingElement = element.getEnclosingElement();
      if (enclosingElement instanceof TypeElement) {
        error(
            element,
            "Field %s#%s is not a valid %s.",
            ((TypeElement) enclosingElement).getQualifiedName(),
            element.getSimpleName(),
            declaredType);
      } else {
        error(
            element,
            "Parameter %s in method/constructor %s#%s is not a valid %s.",
            element.getSimpleName(), //
            ((TypeElement) enclosingElement.getEnclosingElement()).getQualifiedName(), //
            enclosingElement.getSimpleName(),
            declaredType);
      }
      return false;
    }

    TypeMirror firstParameterTypeMirror = declaredType.getTypeArguments().get(0);
    if (firstParameterTypeMirror.getKind() == TypeKind.DECLARED) {
      int size = ((DeclaredType) firstParameterTypeMirror).getTypeArguments().size();
      if (size != 0) {
        Element enclosingElement = element.getEnclosingElement();
        error(
            element,
            "Lazy/Provider %s is not a valid in %s. Lazy/Provider cannot be used on generic types.",
            element.getSimpleName(), //
            enclosingElement.getSimpleName());
        return false;
      }
    }

    return true;
  }

  protected List<ParamInjectionTarget> getParamInjectionTargetList(
      ExecutableElement executableElement) {
    List<ParamInjectionTarget> paramInjectionTargetList = new ArrayList<>();
    for (VariableElement variableElement : executableElement.getParameters()) {
      paramInjectionTargetList.add(createFieldOrParamInjectionTarget(variableElement));
    }
    return paramInjectionTargetList;
  }

  protected List<TypeElement> getExceptionTypes(ExecutableElement methodElement) {
    List<TypeElement> exceptionClassNames = new ArrayList<>();
    for (TypeMirror thrownTypeMirror : methodElement.getThrownTypes()) {
      DeclaredType thrownDeclaredType = (DeclaredType) thrownTypeMirror;
      TypeElement thrownType = (TypeElement) thrownDeclaredType.asElement();
      exceptionClassNames.add(thrownType);
    }

    return exceptionClassNames;
  }

  protected FieldInjectionTarget createFieldOrParamInjectionTarget(
      VariableElement variableElement) {
    final TypeElement memberTypeElement =
        (TypeElement) typeUtils.asElement(variableElement.asType());
    final String memberName = variableElement.getSimpleName().toString();

    ParamInjectionTarget.Kind kind = getParamInjectionTargetKind(variableElement);
    TypeElement kindParameterTypeElement = getInjectedType(variableElement);

    String name = findQualifierName(variableElement);

    return new FieldInjectionTarget(
        memberTypeElement, memberName, kind, kindParameterTypeElement, name);
  }

  /**
   * Retrieves the type of a field or param. The type can be the type of the parameter in the java
   * way (e.g. {@code B b}, type is {@code B}); but it can also be the type of a {@link
   * toothpick.Lazy} or {@link javax.inject.Provider} (e.g. {@code Lazy&lt;B&gt; b}, type is {@code
   * B} not {@code Lazy}).
   *
   * @param variableElement the field or variable element. NOT his type !
   * @return the type has defined above.
   */
  protected TypeElement getInjectedType(VariableElement variableElement) {
    final TypeElement fieldType;
    if (getParamInjectionTargetKind(variableElement) == ParamInjectionTarget.Kind.INSTANCE) {
      fieldType = (TypeElement) typeUtils.asElement(typeUtils.erasure(variableElement.asType()));
    } else {
      fieldType = getKindParameter(variableElement);
    }
    return fieldType;
  }

  protected boolean isExcludedByFilters(TypeElement typeElement) {
    String typeElementName = typeElement.getQualifiedName().toString();
    for (String exclude : toothpickExcludeFilters.split(",")) {
      String regEx = exclude.trim();
      if (typeElementName.matches(regEx)) {
        warning(
            typeElement,
            "The class %s was excluded by filters set at the annotation processor level. "
                + "No factory will be generated by toothpick.",
            typeElement.getQualifiedName());
        return true;
      }
    }
    return false;
  }

  // overrides are simpler in this case as methods can only be package or protected.
  // a method with the same name in the type hierarchy would necessarily mean that
  // the {@code methodElement} would be an override of this method.
  protected boolean isOverride(TypeElement typeElement, ExecutableElement methodElement) {
    TypeElement currentTypeElement = typeElement;
    do {
      if (currentTypeElement != typeElement) {
        List<? extends Element> enclosedElements = currentTypeElement.getEnclosedElements();
        for (Element enclosedElement : enclosedElements) {
          if (enclosedElement.getSimpleName().equals(methodElement.getSimpleName())
              && enclosedElement.getAnnotation(Inject.class) != null
              && enclosedElement.getKind() == ElementKind.METHOD) {
            return true;
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
    return false;
  }

  protected TypeElement getMostDirectSuperClassWithInjectedMembers(
      TypeElement typeElement, boolean onlyParents) {
    TypeElement currentTypeElement = typeElement;
    do {
      if (currentTypeElement != typeElement || !onlyParents) {
        List<? extends Element> enclosedElements = currentTypeElement.getEnclosedElements();
        for (Element enclosedElement : enclosedElements) {
          if ((enclosedElement.getAnnotation(Inject.class) != null
                  && enclosedElement.getKind() == ElementKind.FIELD)
              || (enclosedElement.getAnnotation(Inject.class) != null
                  && enclosedElement.getKind() == ElementKind.METHOD)) {
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

  protected boolean isNonStaticInnerClass(TypeElement typeElement) {
    Element outerClassOrPackage = typeElement.getEnclosingElement();
    if (outerClassOrPackage.getKind() != ElementKind.PACKAGE
        && !typeElement.getModifiers().contains(Modifier.STATIC)) {
      error(
          typeElement,
          "Class %s is a non static inner class. @Inject constructors are not allowed in non static inner classes.",
          typeElement.getQualifiedName());
      return true;
    }
    return false;
  }

  /**
   * Checks if {@code element} has a @SuppressWarning("{@code warningSuppressString}") annotation.
   *
   * @param element the element to check if the warning is suppressed.
   * @param warningSuppressString the value of the SuppressWarning annotation.
   * @return true is the injectable warning is suppressed, false otherwise.
   */
  protected boolean hasWarningSuppressed(Element element, String warningSuppressString) {
    SuppressWarnings suppressWarnings = element.getAnnotation(SuppressWarnings.class);
    if (suppressWarnings != null) {
      for (String value : suppressWarnings.value()) {
        if (value.equalsIgnoreCase(warningSuppressString)) {
          return true;
        }
      }
    }
    return false;
  }

  /**
   * Lookup both {@link javax.inject.Qualifier} and {@link javax.inject.Named} to provide the name
   * of an injection.
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
      TypeElement annotationTypeElement =
          (TypeElement) annotationMirror.getAnnotationType().asElement();
      if (isSameType(annotationTypeElement, "javax.inject.Named")) {
        checkIfAlreadyHasName(element, name);
        name = getValueOfAnnotation(annotationMirror);
      } else if (annotationTypeElement.getAnnotation(javax.inject.Qualifier.class) != null) {
        checkIfAlreadyHasName(element, name);
        name = annotationTypeElement.getQualifiedName().toString();
      }
    }
    return name;
  }

  private boolean isSameType(TypeElement typeElement, String typeName) {
    return isSameType(typeElement.asType(), typeName);
  }

  private boolean isSameType(TypeMirror typeMirror, String typeName) {
    return typeUtils.isSameType(
        typeUtils.erasure(typeMirror),
        typeUtils.erasure(elementUtils.getTypeElement(typeName).asType()));
  }

  private void checkIfAlreadyHasName(VariableElement element, Object name) {
    if (name != null) {
      error(element, "Only one javax.inject.Qualifier annotation is allowed to name injections.");
    }
  }

  private String getValueOfAnnotation(AnnotationMirror annotationMirror) {
    String result = null;
    for (Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> annotationParamEntry :
        annotationMirror.getElementValues().entrySet()) {
      if (annotationParamEntry.getKey().getSimpleName().contentEquals("value")) {
        result = annotationParamEntry.getValue().toString().replaceAll("\"", "");
      }
    }
    return result;
  }

  private boolean isProviderOrLazy(Element element) {
    FieldInjectionTarget.Kind kind = getParamInjectionTargetKind(element);
    return kind == ParamInjectionTarget.Kind.PROVIDER || kind == ParamInjectionTarget.Kind.LAZY;
  }

  private FieldInjectionTarget.Kind getParamInjectionTargetKind(Element variableElement) {
    TypeMirror elementTypeMirror = variableElement.asType();
    if (isSameType(elementTypeMirror, "javax.inject.Provider")) {
      return FieldInjectionTarget.Kind.PROVIDER;
    } else if (isSameType(elementTypeMirror, "toothpick.Lazy")) {
      return FieldInjectionTarget.Kind.LAZY;
    } else {
      Element typeElement = typeUtils.asElement(variableElement.asType());
      if (typeElement.getKind() != ElementKind.CLASS //
          && typeElement.getKind() != ElementKind.INTERFACE //
          && typeElement.getKind() != ElementKind.ENUM) {

        Element enclosingElement = variableElement.getEnclosingElement();
        while (!(enclosingElement instanceof TypeElement)) {
          enclosingElement = enclosingElement.getEnclosingElement();
        }
        error(
            variableElement,
            "Field %s#%s is of type %s which is not supported by Toothpick.",
            ((TypeElement) enclosingElement).getQualifiedName(),
            variableElement.getSimpleName(),
            typeElement);
        return null;
      }
      return FieldInjectionTarget.Kind.INSTANCE;
    }
  }

  private TypeElement getKindParameter(Element element) {
    TypeMirror elementTypeMirror = element.asType();
    TypeMirror firstParameterTypeMirror =
        ((DeclaredType) elementTypeMirror).getTypeArguments().get(0);
    return (TypeElement) typeUtils.asElement(typeUtils.erasure(firstParameterTypeMirror));
  }
}
