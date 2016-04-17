package toothpick.compiler.factory.targets;

import java.util.ArrayList;
import java.util.List;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;

/**
 * Basically all information to create an object / call a constructor of a class.
 */
public final class FactoryInjectionTarget {
  public final List<TypeMirror> parameters = new ArrayList<>();
  public final TypeElement builtClass;
  public final boolean hasSingletonAnnotation;
  public final boolean hasProducesSingletonAnnotation;
  /** true if the class as @Injected members */
  public final TypeElement superClassThatNeedsMemberInjection;

  public FactoryInjectionTarget(TypeElement builtClass, boolean hasSingletonAnnotation, boolean hasProducesSingletonAnnotation,
      TypeElement superClassThatNeedsMemberInjection) {
    this.builtClass = builtClass;
    this.hasSingletonAnnotation = hasSingletonAnnotation;
    this.hasProducesSingletonAnnotation = hasProducesSingletonAnnotation;
    this.superClassThatNeedsMemberInjection = superClassThatNeedsMemberInjection;
  }
}
