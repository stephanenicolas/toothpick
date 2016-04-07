package toothpick.compiler.factory.targets;

import java.util.Collection;

public final class FactoryRegistryInjectionTarget {

  public static final String FACTORY_REGISTRY_NAME = "FactoryRegistry";

  public Collection<FactoryInjectionTarget> factoryInjectionTargetList;
  public String packageName;
  public Collection<String> childrenRegistryPackageNameList;

  public FactoryRegistryInjectionTarget(Collection<FactoryInjectionTarget> factoryInjectionTargetList, String packageName,
      Collection<String> childrenRegistryPackageNameList) {
    this.factoryInjectionTargetList = factoryInjectionTargetList;
    this.packageName = packageName;
    this.childrenRegistryPackageNameList = childrenRegistryPackageNameList;
  }

  public String getFqcn() {
    return packageName + "." + FACTORY_REGISTRY_NAME;
  }
}
