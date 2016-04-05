package toothpick.compiler.targets;

import java.util.ArrayList;
import java.util.List;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;

/**
 * Basically all information to create an object / call a constructor of a class.
 */
public final class MethodInjectionTarget {
  public final List<TypeMirror> parameters = new ArrayList<>();
  public final String methodName;
  public final TypeElement returnType;
  public final boolean hasSingletonAnnotation;
  public final boolean hasProducesSingletonAnnotation;
  /** true if the class as @Injected members */
  public final boolean needsMemberInjection;

  public MethodInjectionTarget(String methodName, TypeElement returnType, boolean hasSingletonAnnotation, boolean hasProducesSingletonAnnotation,
      boolean needsMemberInjection) {
    this.methodName = methodName;
    this.returnType = returnType;
    this.hasSingletonAnnotation = hasSingletonAnnotation;
    this.hasProducesSingletonAnnotation = hasProducesSingletonAnnotation;
    this.needsMemberInjection = needsMemberInjection;
  }
}
