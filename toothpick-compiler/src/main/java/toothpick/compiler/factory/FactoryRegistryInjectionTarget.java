package toothpick.compiler.factory;

import java.util.Collection;

public final class FactoryRegistryInjectionTarget {

  public static final String FACTORY_REGISTRY_NAME = "FactoryRegistry";

  public Collection<ConstructorInjectionTarget> constructorInjectionTargetList;
  public String packageName;
  public Collection<String> childrenRegistryPackageNameList;

  public FactoryRegistryInjectionTarget(Collection<ConstructorInjectionTarget> constructorInjectionTargetList, String packageName,
      Collection<String> childrenRegistryPackageNameList) {
    this.constructorInjectionTargetList = constructorInjectionTargetList;
    this.packageName = packageName;
    this.childrenRegistryPackageNameList = childrenRegistryPackageNameList;
  }

  public String getFqcn() {
    return packageName + "." + FACTORY_REGISTRY_NAME;
  }
}
