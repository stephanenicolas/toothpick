package toothpick.compiler.memberinjector.targets;

import java.util.ArrayList;
import java.util.List;
import javax.lang.model.element.TypeElement;
import toothpick.compiler.common.generators.targets.ParamInjectionTarget;

/**
 * Basically all information to create an object / call a constructor of a class.
 */
public final class MethodInjectionTarget {
  public final List<ParamInjectionTarget> parameters = new ArrayList<>();
  public final TypeElement enclosingClass;
  public final String methodName;
  public final TypeElement returnType;

  public MethodInjectionTarget(TypeElement enclosingClass, String methodName, TypeElement returnType) {
    this.enclosingClass = enclosingClass;
    this.methodName = methodName;
    this.returnType = returnType;
  }
}
