package toothpick.compiler.memberinjector.targets;

import javax.lang.model.element.TypeElement;

public final class FieldInjectionTarget {

  public TypeElement memberClass;
  public final String memberName;
  public final Kind kind;
  public final TypeElement kindParamClass;
  public final Object name;

  public FieldInjectionTarget(TypeElement memberClass, String memberName, Kind kind, TypeElement kindParamClass, Object name) {
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
    FUTURE
  }
}
