package toothpick.compiler.factory;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.TypeVariableName;
import javax.lang.model.element.Modifier;
import toothpick.Factory;
import toothpick.registries.factory.AbstractFactoryRegistry;

public class FactoryRegistryGenerator {

  private FactoryRegistryInjectionTarget factoryRegistryInjectionTarget;

  public FactoryRegistryGenerator(FactoryRegistryInjectionTarget factoryRegistryInjectionTarget) {
    this.factoryRegistryInjectionTarget = factoryRegistryInjectionTarget;
  }

  /*  package toothpick.integration.data;

  import toothpick.Factory;
  import toothpick.registries.factory.AbstractFactoryRegistry;

  public class FactoryRegistry extends AbstractFactoryRegistry {

    @Override public <T> Factory<T> getFactory(Class<T> clazz) {
      switch (clazz.getName()) {
        case "toothpick.integration.data.Bar":
          return (Factory<T>) new Bar$$Factory();
        case "toothpick.integration.data.Foo":
          return (Factory<T>) new Foo$$Factory();
        case "toothpick.integration.data.FooSingleton":
          return (Factory<T>) new FooSingleton$$Factory();
        case "toothpick.integration.data.IFooProvider":
          return (Factory<T>) new IFooProvider$$Factory();
        case "toothpick.integration.data.IFooWithBarProvider":
          return (Factory<T>) new IFooWithBarProvider$$Factory();
        case "toothpick.integration.data.IFooProviderAnnotatedProvidesSingleton":
          return (Factory<T>) new IFooProviderAnnotatedProvidesSingleton$$Factory();
        case "toothpick.integration.data.IFooProviderAnnotatedSingleton":
          return (Factory<T>) new IFooProviderAnnotatedSingleton$$Factory();
        default:
          return getFactoryInChildrenRegistries(clazz);
      }
    }
  }*/

  public String brewJava() {
    // Build class
    TypeSpec.Builder factoryRegistryTypeSpec = TypeSpec.classBuilder(FactoryRegistryInjectionTarget.FACTORY_REGISTRY_NAME)
        .addModifiers(Modifier.PUBLIC)
        //TODO do not use the class but a name, this ties the generator to the AbstractFactoryRegistry
        //and forces up to put it in the toothpick lib vs runtime lib, which is not desirable
        //the runtime package could still be used for running tests..
        .superclass(ClassName.get(AbstractFactoryRegistry.class));

    emitGetFactoryMethod(factoryRegistryTypeSpec);

    JavaFile javaFile = JavaFile.builder(factoryRegistryInjectionTarget.packageName, factoryRegistryTypeSpec.build())
        .addFileComment("Generated code from Dart. Do not modify!")
        .build();
    return javaFile.toString();
  }

  private void emitGetFactoryMethod(TypeSpec.Builder factoryRegistryTypeSpec) {
    TypeVariableName t = TypeVariableName.get("T");
    MethodSpec.Builder getFactoryMethod = MethodSpec.methodBuilder("getFactory")
        .addTypeVariable(t)
        .addModifiers(Modifier.PUBLIC)
        .addParameter(ParameterizedTypeName.get(ClassName.get(Class.class), t), "clazz")
        .returns(ParameterizedTypeName.get(ClassName.get(Factory.class), t));

    StringBuilder switchStatement = new StringBuilder();
    CodeBlock.Builder switchBlockBuilder = CodeBlock.builder()
        .beginControlFlow("switch($L)", "clazz.getName()");

    for (FactoryInjectionTarget factoryInjectionTarget : factoryRegistryInjectionTarget.factoryInjectionTargetList) {
      switchBlockBuilder.add("case ($S):\n", factoryInjectionTarget.targetClass);
      switchBlockBuilder.add("return (Factory<T>) new $L$$$$Factory();\n", ClassName.get(factoryInjectionTarget.classPackage,
          factoryInjectionTarget.className));
    }
    switchBlockBuilder.add("default:\n");
    switchBlockBuilder.add("return getFactoryInChildrenRegistries(clazz);\n");
    switchBlockBuilder.endControlFlow();
    getFactoryMethod.addCode(switchBlockBuilder.build());
    factoryRegistryTypeSpec.addMethod(getFactoryMethod.build());
  }


  public String getFqcn() {
    return factoryRegistryInjectionTarget.getFqcn();
  }
}
