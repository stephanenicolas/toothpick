package toothpick.compiler.factory.targets;

import java.util.ArrayList;
import java.util.List;
import javax.lang.model.element.TypeElement;
import toothpick.compiler.common.generators.targets.ParamInjectionTarget;

/**
 * Basically all information to create an object / call a constructor of a class.
 */
public final class ConstructorInjectionTarget {
  public final List<ParamInjectionTarget> parameters = new ArrayList<>();
  public final TypeElement builtClass;
  public String scopeName;
  public final boolean hasProducesSingletonAnnotation;
  /** true if the class as @Injected members */
  public final TypeElement superClassThatNeedsMemberInjection;

  public ConstructorInjectionTarget(TypeElement builtClass, String scopeName, boolean hasProducesSingletonAnnotation,
      TypeElement superClassThatNeedsMemberInjection) {
    this.builtClass = builtClass;
    this.scopeName = scopeName;
    this.hasProducesSingletonAnnotation = hasProducesSingletonAnnotation;
    this.superClassThatNeedsMemberInjection = superClassThatNeedsMemberInjection;
  }
}
