package toothpick.compiler.registry.targets;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import javax.lang.model.element.TypeElement;
import toothpick.compiler.registry.generators.RegistryGenerator;
import toothpick.registries.FactoryRegistry;
import toothpick.registries.MemberInjectorRegistry;

/**
 * Stores the information needed by {@link RegistryGenerator} to generate a Registry.
 *
 * See {@link FactoryRegistry} and {@link MemberInjectorRegistry} for Registry types.
 */
public class RegistryInjectionTarget {

  private static final String REGISTRY_NAME = "%sRegistry";
  private static final String GETTER_NAME = "get%s";
  private static final String CHILDREN_GETTER_NAME = "get%sInChildrenRegistries";

  public Class<?> type; // Factory.class
  public Class<?> superClass; // toothpick.registries.factory.AbstractFactoryRegistry
  public String packageName;
  public List<String> childrenRegistryPackageNameList;
  public List<TypeElement> injectionTargetList;

  public String registryName;
  public String getterName;
  public String childrenGetterName;

  public RegistryInjectionTarget(Class<?> type, Class<?> superClass, String packageName,
      List<String> childrenRegistryPackageNameList, List<TypeElement> injectionTargetList) {
    this.type = type;
    this.superClass = superClass;
    this.packageName = packageName;
    this.childrenRegistryPackageNameList = childrenRegistryPackageNameList;
    this.injectionTargetList = injectionTargetList;
    Collections.sort(injectionTargetList, new TypeElementComparator());

    String typeSimpleName = type.getSimpleName();
    this.registryName = String.format(REGISTRY_NAME, typeSimpleName);
    this.getterName = String.format(GETTER_NAME, typeSimpleName);
    this.childrenGetterName = String.format(CHILDREN_GETTER_NAME, typeSimpleName);
  }

  public String getFqcn() {
    return packageName + "." + registryName;
  }

  private static class TypeElementComparator implements Comparator<TypeElement> {
    @Override
    public int compare(TypeElement t0, TypeElement t1) {
      return t0.getQualifiedName().toString().compareTo(t1.getQualifiedName().toString());
    }
  }
}
