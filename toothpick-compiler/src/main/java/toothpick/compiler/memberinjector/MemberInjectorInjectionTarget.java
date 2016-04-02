package toothpick.compiler.memberinjector;

public final class MemberInjectorInjectionTarget {

  public final String targetClassPackage;
  public final String targetClassName;
  public final String targetClass;
  public final String memberClass;
  public final String memberName;
  //TODO identify if we need to call a super class memberInjector, finds its class name.

  public MemberInjectorInjectionTarget(String targetClassPackage, String memberClassName, String targetClass, String memberClass, String memberName) {
    this.targetClassPackage = targetClassPackage;
    this.targetClassName = memberClassName;
    this.targetClass = targetClass;
    this.memberClass = memberClass;
    this.memberName = memberName;
  }
}
