package toothpick.compiler;

import java.io.IOException;
import java.io.Writer;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.JavaFileObject;

import static javax.tools.Diagnostic.Kind.ERROR;
import static javax.tools.Diagnostic.Kind.WARNING;

/**
 * Base processor class.
 */
public abstract class ToothpickProcessor extends AbstractProcessor {

  /** The name of the {@link javax.inject.Inject} annotation class that triggers {@code ToothpickProcessor}s. */
  public static final String INJECT_ANNOTATION_CLASS_NAME = "javax.inject.Inject";

  /** The name annotation processor option to declare in which package a registry should be generated.
   * If this parameter is not passed, no registry is generated.*/
  public static final String PARAMETER_REGISTRY_PACKAGE_NAME = "toothpick_registry_package_name";

  /** The name annotation processor option to declare in which packages reside the registries used by the generated registry, if it is created.
   * @see #PARAMETER_REGISTRY_PACKAGE_NAME */
  public static final String PARAMETER_REGISTRY_CHILDREN_PACKAGE_NAMES = "toothpick_registry_children_package_names";

  protected Elements elementUtils;
  protected Types typeUtils;
  protected Filer filer;

  protected String toothpickRegistryPackageName;
  protected List<String> toothpickRegistryChildrenPackageNameList;

  @Override public synchronized void init(ProcessingEnvironment processingEnv) {
    super.init(processingEnv);

    elementUtils = processingEnv.getElementUtils();
    typeUtils = processingEnv.getTypeUtils();
    filer = processingEnv.getFiler();
  }

  protected void writeToFile(CodeGenerator codeGenerator, String fileDescription, Element... originatingElements) {
    Writer writer = null;

    try {
      JavaFileObject jfo = filer.createSourceFile(codeGenerator.getFqcn(), originatingElements);
      writer = jfo.openWriter();
      writer.write(codeGenerator.brewJava());
    } catch (IOException e) {
      error("Error writing %s file: %s", fileDescription, e.getMessage());
    } finally {
      if (writer != null) {
        try {
          writer.close();
        } catch (IOException e) {
          error("Error closing %s file: %s", fileDescription, e.getMessage());
        }
      }
    }
  }

  /**
   * Reads both annotation compilers {@link ToothpickProcessor#PARAMETER_REGISTRY_PACKAGE_NAME} and
   * {@link ToothpickProcessor#PARAMETER_REGISTRY_CHILDREN_PACKAGE_NAMES} options from the arguments
   * passed to the processor.
   * @return true if toothpickRegistryPackageName is defined, false otherwise.
   */
  protected boolean readParameters() {
    toothpickRegistryPackageName = processingEnv.getOptions().get(PARAMETER_REGISTRY_PACKAGE_NAME);
    if (toothpickRegistryPackageName == null) {
     warning("No option -Atoothpick_registry_package_name was passed to the compiler."
          + " No registries are generated. Will fallback on reflection at runtime to find factories.");
      return false;
    }

    String toothpickRegistryChildrenPackageNames = processingEnv.getOptions().get(PARAMETER_REGISTRY_CHILDREN_PACKAGE_NAMES);
    toothpickRegistryChildrenPackageNameList = Collections.EMPTY_LIST;
    if (toothpickRegistryChildrenPackageNames != null) {
      toothpickRegistryChildrenPackageNameList = Arrays.asList(toothpickRegistryChildrenPackageNames.split(":"));
    }

    return true;
  }

  protected String getPackageName(TypeElement type) {
    return elementUtils.getPackageOf(type).getQualifiedName().toString();
  }

  protected static String getClassName(TypeElement type, String packageName) {
    int packageLen = packageName.length() + 1;
    return type.getQualifiedName().toString().substring(packageLen).replace('.', '$');
  }

  /**
   * Returns {@code true} if the an annotation is found on the given element with the given class
   * name (not fully qualified).
   */
  protected static boolean hasAnnotationWithName(Element element, String simpleName) {
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

}
