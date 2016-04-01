package toothpick.compiler.factory;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.inject.Inject;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.JavaFileObject;

import static javax.lang.model.element.Modifier.PRIVATE;
import static javax.tools.Diagnostic.Kind.ERROR;

@SupportedAnnotationTypes({ "javax.inject.Inject" }) public class FactoryProcessor extends AbstractProcessor {

  private Elements elementUtils;
  private Types typeUtils;
  private Filer filer;

  @Override public synchronized void init(ProcessingEnvironment processingEnv) {
    super.init(processingEnv);
    System.out.println("coucou");

    elementUtils = processingEnv.getElementUtils();
    typeUtils = processingEnv.getTypeUtils();
    filer = processingEnv.getFiler();
  }

  @Override public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
    System.out.println("coucou");

    Map<TypeElement, FactoryInjectionTarget> targetClassMap = findAndParseTargets(roundEnv);

    for (Map.Entry<TypeElement, FactoryInjectionTarget> entry : targetClassMap.entrySet()) {
      TypeElement typeElement = entry.getKey();
      FactoryInjectionTarget factoryInjectionTarget = entry.getValue();

      Writer writer = null;
      // Generate the ExtraInjector
      try {
        FactoryGenerator factoryGenerator = new FactoryGenerator(factoryInjectionTarget);
        JavaFileObject jfo = filer.createSourceFile(factoryGenerator.getFqcn(), typeElement);
        writer = jfo.openWriter();
        writer.write(factoryGenerator.brewJava());
      } catch (IOException e) {
        error(typeElement, "Unable to write factory for type %s: %s", typeElement, e.getMessage());
      } finally {
        if (writer != null) {
          try {
            writer.close();
          } catch (IOException e) {
            error(typeElement, "Unable to close factory source file for type %s: %s", typeElement, e.getMessage());
          }
        }
      }
    }

    return true;
  }

  private Map<TypeElement, FactoryInjectionTarget> findAndParseTargets(RoundEnvironment roundEnv) {
    Map<TypeElement, FactoryInjectionTarget> targetClassMap = new LinkedHashMap<>();

    for (Element element : roundEnv.getElementsAnnotatedWith(Inject.class)) {
      if (element.getKind() == ElementKind.CONSTRUCTOR) {
        try {
          parseInject(element, targetClassMap);
        } catch (Exception e) {
          StringWriter stackTrace = new StringWriter();
          e.printStackTrace(new PrintWriter(stackTrace));

          error(element, "Unable to generate factory when parsing @Inject.\n\n%s", stackTrace.toString());
        }
      }
    }

    return targetClassMap;
  }

  private void parseInject(Element element, Map<TypeElement, FactoryInjectionTarget> targetClassMap) {
    TypeElement enclosingElement = (TypeElement) element.getEnclosingElement();

    // Verify common generated code restrictions.
    if (!isValidInjectConstructor(element)) {
      return;
    }

    // Another constructor already used for the class.
    if (targetClassMap.containsKey(enclosingElement)) {
      throw new IllegalStateException(
          String.format("@%s class %s must not have more than one " + "annotated constructor.", Inject.class.getSimpleName(),
              element.getSimpleName()));
    }

    targetClassMap.put(enclosingElement, createInjectionTarget(element));
  }

  private boolean isValidInjectConstructor(Element element) {
    boolean valid = true;
    TypeElement enclosingElement = (TypeElement) element.getEnclosingElement();

    // Verify modifiers.
    Set<Modifier> modifiers = element.getModifiers();
    if (modifiers.contains(PRIVATE)) {
      error(element, "@%s constructors must not be private. (%s)", Inject.class.getSimpleName(), enclosingElement.getQualifiedName());
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

  private FactoryInjectionTarget createInjectionTarget(Element element) {
    TypeElement enclosingElement = (TypeElement) element.getEnclosingElement();

    final String classPackage = getPackageName(enclosingElement);
    final String className = getClassName(enclosingElement, classPackage);
    final String targetClass = enclosingElement.getQualifiedName().toString();
    final boolean hasSingletonAnnotation = hasAnnotationWithName(enclosingElement, "Singleton");
    final boolean hasProducesSingletonAnnotation = hasAnnotationWithName(enclosingElement, "ProvidesSingleton");
    boolean needsMemberInjection = needsMemberInjection(enclosingElement);

    FactoryInjectionTarget factoryInjectionTarget =
        new FactoryInjectionTarget(classPackage, className, targetClass, hasSingletonAnnotation, hasProducesSingletonAnnotation,
            needsMemberInjection);
    addParameters(element, factoryInjectionTarget);

    return factoryInjectionTarget;
  }

  private boolean needsMemberInjection(TypeElement enclosingElement) {
    boolean needsMemberInjection = false;
    TypeElement currentTypeElement = enclosingElement;
    while (!"java.lang.Object".equals(currentTypeElement.getQualifiedName().toString())) {
      List<? extends Element> enclosedElements = currentTypeElement.getEnclosedElements();
      for (Element enclosedElement : enclosedElements) {
        if (enclosedElement.getAnnotation(Inject.class) != null && enclosedElement.getKind() == ElementKind.FIELD) {
          needsMemberInjection = true;
          break;
        }
      }
      currentTypeElement = (TypeElement) ((DeclaredType) currentTypeElement.getSuperclass()).asElement();
    }
    return needsMemberInjection;
  }

  private void addParameters(Element element, FactoryInjectionTarget factoryInjectionTarget) {
    ExecutableElement executableElement = (ExecutableElement) element;

    for (VariableElement variableElement : executableElement.getParameters()) {
      factoryInjectionTarget.parameters.add(variableElement.asType());
    }
  }

  private String getPackageName(TypeElement type) {
    return elementUtils.getPackageOf(type).getQualifiedName().toString();
  }

  private static String getClassName(TypeElement type, String packageName) {
    int packageLen = packageName.length() + 1;
    return type.getQualifiedName().toString().substring(packageLen).replace('.', '$');
  }

  /**
   * Returns {@code true} if the an annotation is found on the given element with the given class
   * name (not fully qualified).
   */
  private static boolean hasAnnotationWithName(Element element, String simpleName) {
    for (AnnotationMirror mirror : element.getAnnotationMirrors()) {
      final Element annnotationElement = mirror.getAnnotationType().asElement();
      String annotationName = annnotationElement.getSimpleName().toString();
      if (simpleName.equals(annotationName)) {
        return true;
      }
    }
    return false;
  }

  private void error(Element element, String message, Object... args) {
    processingEnv.getMessager().printMessage(ERROR, String.format(message, args), element);
  }
}
