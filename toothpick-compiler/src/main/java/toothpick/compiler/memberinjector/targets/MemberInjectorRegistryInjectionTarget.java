package toothpick.compiler.memberinjector.targets;

import java.util.Collection;
import javax.lang.model.element.TypeElement;

public final class MemberInjectorRegistryInjectionTarget {

  public static final String MEMBER_INJECTOR_REGISTRY_NAME = "MemberInjectorRegistry";

  public Collection<TypeElement> memberInjectionTargetList;
  public String packageName;
  public Collection<String> childrenRegistryPackageNameList;

  public MemberInjectorRegistryInjectionTarget(Collection<TypeElement> memberInjectionTargetList, String packageName,
      Collection<String> childrenRegistryPackageNameList) {
    this.memberInjectionTargetList = memberInjectionTargetList;
    this.packageName = packageName;
    this.childrenRegistryPackageNameList = childrenRegistryPackageNameList;
  }

  public String getFqcn() {
    return packageName + "." + MEMBER_INJECTOR_REGISTRY_NAME;
  }
}
