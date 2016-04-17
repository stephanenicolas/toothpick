package toothpick.compiler.common.generators.targets;

import javax.lang.model.element.TypeElement;

public class ParamInjectionTarget {

  public TypeElement memberClass;
  public final String memberName;
  public final Kind kind;
  public final TypeElement kindParamClass;
  public final Object name;

  public ParamInjectionTarget(TypeElement memberClass, String memberName, Kind kind, TypeElement kindParamClass, Object name) {
    this.memberClass = memberClass;
    this.memberName = memberName;
    this.kind = kind;
    this.kindParamClass = kindParamClass;
    this.name = name;
  }

  public enum Kind {
    INSTANCE,
    PROVIDER,
    LAZY,
  }
}
