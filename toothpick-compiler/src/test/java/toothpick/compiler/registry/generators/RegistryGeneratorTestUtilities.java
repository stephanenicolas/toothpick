package toothpick.compiler.registry.generators;

public final class RegistryGeneratorTestUtilities {
  private RegistryGeneratorTestUtilities() {
  }

  public static void setInjectionTarjetsPerGetterMethod(int injectionTarjetsPerMethod) {
    RegistryGenerator.injectionTargetsPerGetterMethod = injectionTarjetsPerMethod;
  }
}
