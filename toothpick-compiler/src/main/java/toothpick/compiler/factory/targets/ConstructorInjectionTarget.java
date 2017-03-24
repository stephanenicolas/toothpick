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
  public final boolean hasScopeInstancesAnnotation;
  /** true if the class as @Injected members */
  public final TypeElement superClassThatNeedsMemberInjection;
  public boolean throwsThrowable;

  public ConstructorInjectionTarget(TypeElement builtClass, String scopeName, boolean hasScopeInstancesAnnotation,
      TypeElement superClassThatNeedsMemberInjection) {
    this.builtClass = builtClass;
    this.scopeName = scopeName;
    this.hasScopeInstancesAnnotation = hasScopeInstancesAnnotation;
    this.superClassThatNeedsMemberInjection = superClassThatNeedsMemberInjection;
  }
}
