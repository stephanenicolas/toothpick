package toothpick.compiler.factory.generators;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.TypeVariableName;
import javax.lang.model.element.Modifier;
import toothpick.Factory;
import toothpick.compiler.CodeGenerator;
import toothpick.compiler.factory.FactoryProcessor;
import toothpick.compiler.factory.targets.FactoryRegistryInjectionTarget;
import toothpick.compiler.factory.targets.FactoryInjectionTarget;
import toothpick.registries.FactoryRegistry;
import toothpick.registries.factory.AbstractFactoryRegistry;

/**
 * Generates a {@link FactoryRegistry} for a given {@link FactoryRegistryInjectionTarget}.
 *
 * @see FactoryProcessor for how to generate a registry via annotation processor options.
 */
public class FactoryRegistryGenerator implements CodeGenerator {

  private FactoryRegistryInjectionTarget factoryRegistryInjectionTarget;

  public FactoryRegistryGenerator(FactoryRegistryInjectionTarget factoryRegistryInjectionTarget) {
    this.factoryRegistryInjectionTarget = factoryRegistryInjectionTarget;
  }

  public String brewJava() {
    // Build class
    TypeSpec.Builder factoryRegistryTypeSpec =
        TypeSpec.classBuilder(FactoryRegistryInjectionTarget.FACTORY_REGISTRY_NAME).addModifiers(Modifier.PUBLIC, Modifier.FINAL)
            //TODO do not use the class but a name, this ties the generator to the AbstractFactoryRegistry
            //and forces up to put it in the toothpick lib vs runtime lib, which is not desirable
            //the runtime package could still be used for running tests..
            .superclass(ClassName.get(AbstractFactoryRegistry.class));

    emitConstructor(factoryRegistryTypeSpec);
    emitGetFactoryMethod(factoryRegistryTypeSpec);

    JavaFile javaFile = JavaFile.builder(factoryRegistryInjectionTarget.packageName, factoryRegistryTypeSpec.build())
        .addFileComment("Generated code from Dart. Do not modify!")
        .build();
    return javaFile.toString();
  }

  private void emitConstructor(TypeSpec.Builder factoryRegistryTypeSpec) {
    MethodSpec.Builder constructor = MethodSpec.constructorBuilder().addModifiers(Modifier.PUBLIC);

    CodeBlock.Builder iterateChildAddFactoryRegistryBlock = CodeBlock.builder();
    for (String childPackageName : factoryRegistryInjectionTarget.childrenRegistryPackageNameList) {
      ClassName factoryRegistryClassName = ClassName.get(childPackageName, FactoryRegistryInjectionTarget.FACTORY_REGISTRY_NAME);
      iterateChildAddFactoryRegistryBlock.addStatement("addChildRegistry($L)", factoryRegistryClassName);
    }

    constructor.addCode(iterateChildAddFactoryRegistryBlock.build());
    factoryRegistryTypeSpec.addMethod(constructor.build());
  }

  private void emitGetFactoryMethod(TypeSpec.Builder factoryRegistryTypeSpec) {
    TypeVariableName t = TypeVariableName.get("T");
    MethodSpec.Builder getFactoryMethod = MethodSpec.methodBuilder("getFactory")
        .addTypeVariable(t)
        .addModifiers(Modifier.PUBLIC)
        .addParameter(ParameterizedTypeName.get(ClassName.get(Class.class), t), "clazz")
        .returns(ParameterizedTypeName.get(ClassName.get(Factory.class), t));

    CodeBlock.Builder switchBlockBuilder = CodeBlock.builder().beginControlFlow("switch($L)", "clazz.getName()");

    for (FactoryInjectionTarget factoryInjectionTarget : factoryRegistryInjectionTarget.factoryInjectionTargetList) {
      switchBlockBuilder.add("case ($S):\n", factoryInjectionTarget.builtClass.getQualifiedName().toString());
      switchBlockBuilder.addStatement("return (Factory<T>) new $L$$$$Factory()", ClassName.get(factoryInjectionTarget.builtClass));
    }
    switchBlockBuilder.add("default:\n");
    switchBlockBuilder.addStatement("return getFactoryInChildrenRegistries(clazz)");
    switchBlockBuilder.endControlFlow();
    getFactoryMethod.addCode(switchBlockBuilder.build());
    factoryRegistryTypeSpec.addMethod(getFactoryMethod.build());
  }

  @Override public String getFqcn() {
    return factoryRegistryInjectionTarget.getFqcn();
  }
}
