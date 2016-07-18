package toothpick.compiler.registry.generators;

public final class RegistryGeneratorTestUtilities {
  private RegistryGeneratorTestUtilities() {
  }

  public static void setInjectionTarjetsPerGetterMethod(int injectionTarjetsPerMethod) {
    RegistryGenerator.INJECTION_TARGETS_PER_GETTER_METHOD = injectionTarjetsPerMethod;
  }
}
