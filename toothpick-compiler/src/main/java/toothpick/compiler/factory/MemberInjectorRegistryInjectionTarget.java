package toothpick.compiler.factory;

import java.util.Collection;

public final class MemberInjectorRegistryInjectionTarget {

  public static final String MEMBER_INJECTOR_REGISTRY_NAME = "MemberInjectorRegistry";

  public Collection<FactoryInjectionTarget> factoryInjectionTargetList;
  public String packageName;
  public Collection<String> childrenRegistryPackageNameList;

  public MemberInjectorRegistryInjectionTarget(Collection<FactoryInjectionTarget> factoryInjectionTargetList, String packageName,
      Collection<String> childrenRegistryPackageNameList) {
    this.factoryInjectionTargetList = factoryInjectionTargetList;
    this.packageName = packageName;
    this.childrenRegistryPackageNameList = childrenRegistryPackageNameList;
  }

  public String getFqcn() {
    return packageName + "." + MEMBER_INJECTOR_REGISTRY_NAME;
  }
}
