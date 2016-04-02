package toothpick.compiler.memberinjector;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
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
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.JavaFileObject;
import toothpick.MemberInjector;

import static javax.lang.model.element.Modifier.PRIVATE;
import static javax.tools.Diagnostic.Kind.ERROR;

/**
 * Same as {@link FactoryProcessor} but for {@link MemberInjector} classes.
 * Annotation processor arguments are shared with the {@link FactoryProcessor}.
 *
 * @see FactoryProcessor
 */
@SupportedAnnotationTypes({ "javax.inject.Inject" }) public class MemberInjectorProcessor extends AbstractProcessor {

  private Elements elementUtils;
  private Types typeUtils;
  private Filer filer;

  @Override public synchronized void init(ProcessingEnvironment processingEnv) {
    super.init(processingEnv);

    elementUtils = processingEnv.getElementUtils();
    typeUtils = processingEnv.getTypeUtils();
    filer = processingEnv.getFiler();
  }

  @Override public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
    Map<TypeElement, List<MemberInjectorInjectionTarget>> targetClassMap = findAndParseTargets(roundEnv);

    for (Map.Entry<TypeElement, List<MemberInjectorInjectionTarget>> entry : targetClassMap.entrySet()) {
      TypeElement typeElement = entry.getKey();
      List<MemberInjectorInjectionTarget> memberInjectorInjectionTargetList = entry.getValue();

      Writer writer = null;
      // Generate the ExtraInjector
      try {
        MemberInjectorGenerator memberInjectorGenerator = new MemberInjectorGenerator(memberInjectorInjectionTargetList);
        JavaFileObject jfo = filer.createSourceFile(memberInjectorGenerator.getFqcn(), typeElement);
        writer = jfo.openWriter();
        writer.write(memberInjectorGenerator.brewJava());
      } catch (IOException e) {
        error(typeElement, "Unable to write MemberInjector for type %s: %s", typeElement, e.getMessage());
      } finally {
        if (writer != null) {
          try {
            writer.close();
          } catch (IOException e) {
            error(typeElement, "Unable to close MemberInjector source file for type %s: %s", typeElement, e.getMessage());
          }
        }
      }
    }

    return false;
  }

  private Map<TypeElement, List<MemberInjectorInjectionTarget>> findAndParseTargets(RoundEnvironment roundEnv) {
    Map<TypeElement, List<MemberInjectorInjectionTarget>> targetClassMap = new LinkedHashMap<>();

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

    return targetClassMap;
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

    return new MemberInjectorInjectionTarget(targetClassPackage, targetClassName, targetClass, memberClassPackage, memberClassName, memberName);
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
