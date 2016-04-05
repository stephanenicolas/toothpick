package toothpick.compiler.factory;

import java.util.ArrayList;
import java.util.List;
import javax.lang.model.type.TypeMirror;

/**
 * Basically all information to create an object / call a constructor of a class.
 */
public final class FactoryInjectionTarget {
  public final List<TypeMirror> parameters = new ArrayList<>();
  public final String classPackage;
  public final String className;
  public final boolean hasSingletonAnnotation;
  public final boolean hasProducesSingletonAnnotation;
  /** true if the class as @Injected members */
  public final boolean needsMemberInjection;

  public FactoryInjectionTarget(String classPackage, String className, boolean hasSingletonAnnotation, boolean hasProducesSingletonAnnotation,
      boolean needsMemberInjection) {
    this.classPackage = classPackage;
    this.className = className;
    this.hasSingletonAnnotation = hasSingletonAnnotation;
    this.hasProducesSingletonAnnotation = hasProducesSingletonAnnotation;
    this.needsMemberInjection = needsMemberInjection;
  }
}
