package toothpick.compiler.factory;

public class FactoryRegistryGenerator {

  private static final String FACTORY_SUFFIX = "$$Factory";

  private FactoryInjectionTarget factoryInjectionTarget;

  public FactoryRegistryGenerator(FactoryInjectionTarget factoryInjectionTarget) {
    this.factoryInjectionTarget = factoryInjectionTarget;
  }

  public String brewJava() {
    /*TypeSpec.Builder injectorTypeSpec =
        TypeSpec.classBuilder(injectionTarget.className + FACTORY_SUFFIX)
            .addModifiers(Modifier.PUBLIC);
    emitInject(injectorTypeSpec);
    JavaFile javaFile = JavaFile.builder(target.classPackage, injectorTypeSpec.build())
        .addFileComment("Generated code from Dart. Do not modify!")
        .build();
    return javaFile.toString();*/
    return "";
  }

  public String getFqcn() {
    return factoryInjectionTarget.getFqcn() + FACTORY_SUFFIX;
  }
}
