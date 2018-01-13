package toothpick.compiler.registry.generators;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.TypeVariableName;

import java.util.HashMap;
import java.util.Map;

import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Types;

import toothpick.compiler.common.generators.CodeGenerator;
import toothpick.compiler.registry.targets.RegistryInjectionTarget;
import toothpick.registries.FactoryRegistry;
import toothpick.registries.MemberInjectorRegistry;

/**
 * Generates a Registry for a given {@link RegistryInjectionTarget} in a way that it works after obfuscation.
 *
 * See {@link FactoryRegistry} and {@link MemberInjectorRegistry} for Registry types.
 */
public class ObfuscationFriendlyRegistryGenerator extends CodeGenerator {

  private static final String FACTORIES_FIELD_NAME = "factories";
  private final RegistryInjectionTarget registryInjectionTarget;

  public ObfuscationFriendlyRegistryGenerator(RegistryInjectionTarget registryInjectionTarget, Types types) {
    super(types);
    this.registryInjectionTarget = registryInjectionTarget;
  }

  @Override
  public String brewJava() {
    TypeSpec.Builder registryTypeSpec = TypeSpec.classBuilder(registryInjectionTarget.registryName)
        .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
        .superclass(ClassName.get(registryInjectionTarget.superClass));

    emitMapField(registryTypeSpec);
    emitConstructor(registryTypeSpec);
    emitGetterMethod(registryTypeSpec);

    JavaFile javaFile = JavaFile.builder(registryInjectionTarget.packageName, registryTypeSpec.build())
        .addFileComment("Generated code from Toothpick. Do not modify!")
        .build();

    return javaFile.toString();
  }

  private void emitMapField(TypeSpec.Builder registryTypeSpec) {
    FieldSpec fieldSpec =
            FieldSpec.builder(ParameterizedTypeName.get(Map.class, String.class, registryInjectionTarget.type),
                              FACTORIES_FIELD_NAME,
                              Modifier.PRIVATE, Modifier.FINAL)
                              .initializer("new $T<>()", HashMap.class)
                              .build();
    registryTypeSpec.addField(fieldSpec);
  }

  private void emitConstructor(TypeSpec.Builder registryTypeSpec) {
    MethodSpec.Builder constructor = MethodSpec.constructorBuilder().addModifiers(Modifier.PUBLIC);
    addCodeForAddingChildRegistries(constructor);
    addMapFillingCode(constructor);
    registryTypeSpec.addMethod(constructor.build());
  }

  private void addCodeForAddingChildRegistries(MethodSpec.Builder constructor) {
    CodeBlock.Builder iterateChildAddRegistryBlock = CodeBlock.builder();
    for (String childPackageName : registryInjectionTarget.childrenRegistryPackageNameList) {
      ClassName registryClassName = ClassName.get(childPackageName, registryInjectionTarget.registryName);
      iterateChildAddRegistryBlock.addStatement("addChildRegistry(new $L())", registryClassName);
    }

    constructor.addCode(iterateChildAddRegistryBlock.build());
  }

  private void addMapFillingCode(MethodSpec.Builder constructor) {
    String typeSimpleName = registryInjectionTarget.type.getSimpleName();
    for (TypeElement typeElement : registryInjectionTarget.injectionTargetList) {
      constructor.addStatement("$L.put($S, new $L$$$$$L())",
              FACTORIES_FIELD_NAME,  getGeneratedFQNClassName(typeElement), getGeneratedFQNClassName(typeElement), typeSimpleName);
    }
  }

  private void emitGetterMethod(TypeSpec.Builder registryTypeSpec) {
    TypeVariableName t = TypeVariableName.get("T");
    MethodSpec.Builder getMethod = MethodSpec.methodBuilder(registryInjectionTarget.getterName)
        .addTypeVariable(t)
        .addAnnotation(Override.class)
        .addModifiers(Modifier.PUBLIC)
        .addParameter(ParameterizedTypeName.get(ClassName.get(Class.class), t), "clazz")
        .returns(ParameterizedTypeName.get(ClassName.get(registryInjectionTarget.type), t));

    getMethod.addStatement("$T factory = $L.get(clazz.getName())", registryInjectionTarget.type, FACTORIES_FIELD_NAME);

    CodeBlock.Builder blockBuilder = CodeBlock.builder().beginControlFlow("if (factory != null)");
    blockBuilder.addStatement("return ($T<$L>) factory", registryInjectionTarget.type, t);
    blockBuilder.endControlFlow();

    getMethod.addCode(blockBuilder.build());
    getMethod.addStatement("return $L(clazz)", registryInjectionTarget.childrenGetterName);
    registryTypeSpec.addMethod(getMethod.build());
  }

  @Override
  public String getFqcn() {
    return registryInjectionTarget.getFqcn();
  }
}
