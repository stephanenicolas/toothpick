package toothpick.compiler.targets;

import javax.lang.model.element.TypeElement;

public final class FieldInjectionTarget {

  public TypeElement memberClass;
  public final String memberName;
  public final Kind kind;
  public final TypeElement kindParamClass;

  public FieldInjectionTarget(TypeElement memberClass, String memberName, Kind kind, TypeElement kindParamClass) {
    this.memberClass = memberClass;
    this.memberName = memberName;
    this.kind = kind;
    this.kindParamClass = kindParamClass;
  }

  public enum Kind {
    INSTANCE,
    PROVIDER,
    LAZY,
    FUTURE
  }
}
