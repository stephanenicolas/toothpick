package toothpick.compiler.memberinjector.targets;

import javax.lang.model.element.TypeElement;
import toothpick.compiler.common.generators.targets.ParamInjectionTarget;

public final class FieldInjectionTarget extends ParamInjectionTarget {
  public FieldInjectionTarget(TypeElement memberClass, String memberName, Kind kind, TypeElement kindParamClass, Object name) {
    super(memberClass, memberName, kind, kindParamClass, name);
  }
}
