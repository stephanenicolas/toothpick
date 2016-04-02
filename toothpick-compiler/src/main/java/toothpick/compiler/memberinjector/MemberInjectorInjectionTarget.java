package toothpick.compiler.memberinjector;

public final class MemberInjectorInjectionTarget {

  public final String targetClassPackage;
  public final String targetClassName;
  public final String targetClass;
  public final String memberClassPackage;
  public final String memberClassName;
  public final String memberName;
  //TODO identify if we need to call a super class memberInjector, finds its class name.

  public MemberInjectorInjectionTarget(String targetClassPackage, String targetClassName, String targetClass, String memberClassPackage,
      String memberClassName, String memberName) {
    this.targetClassPackage = targetClassPackage;
    this.targetClassName = targetClassName;
    this.targetClass = targetClass;
    this.memberClassPackage = memberClassPackage;
    this.memberClassName = memberClassName;
    this.memberName = memberName;
  }
}
