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
  public final TypeElement enclosingClass;
  public final String methodName;
  public final TypeElement returnType;

  public MethodInjectionTarget(TypeElement enclosingClass, String methodName, TypeElement returnType) {
    this.enclosingClass = enclosingClass;
    this.methodName = methodName;
    this.returnType = returnType;
  }
}
