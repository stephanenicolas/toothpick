package toothpick.compiler.targets;

import javax.lang.model.element.TypeElement;

public final class FieldInjectionTarget {

  public final TypeElement targetClass;
  public TypeElement memberClass;
  public final String memberName;
  public final TypeElement superClassThatNeedsInjection;
  public final Kind kind;
  public final TypeElement kindParamClass;

  public FieldInjectionTarget(TypeElement targetClass, TypeElement memberClass, String memberName, TypeElement superClassThatNeedsInjection,
      Kind kind, TypeElement kindParamClass) {
    this.targetClass = targetClass;
    this.memberClass = memberClass;
    this.memberName = memberName;
    this.superClassThatNeedsInjection = superClassThatNeedsInjection;
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
