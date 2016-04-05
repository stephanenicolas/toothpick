package toothpick.compiler.factory;

import java.util.Collection;
import toothpick.compiler.targets.ConstructorInjectionTarget;

public final class MemberInjectorRegistryInjectionTarget {

  public static final String MEMBER_INJECTOR_REGISTRY_NAME = "MemberInjectorRegistry";

  public Collection<ConstructorInjectionTarget> constructorInjectionTargetList;
  public String packageName;
  public Collection<String> childrenRegistryPackageNameList;

  public MemberInjectorRegistryInjectionTarget(Collection<ConstructorInjectionTarget> constructorInjectionTargetList, String packageName,
      Collection<String> childrenRegistryPackageNameList) {
    this.constructorInjectionTargetList = constructorInjectionTargetList;
    this.packageName = packageName;
    this.childrenRegistryPackageNameList = childrenRegistryPackageNameList;
  }

  public String getFqcn() {
    return packageName + "." + MEMBER_INJECTOR_REGISTRY_NAME;
  }
}
