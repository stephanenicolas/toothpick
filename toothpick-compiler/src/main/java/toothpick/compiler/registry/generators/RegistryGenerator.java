package toothpick.compiler.registry.generators;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.TypeVariableName;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Types;

import toothpick.compiler.common.generators.CodeGenerator;
import toothpick.compiler.registry.targets.RegistryInjectionTarget;
import toothpick.registries.FactoryRegistry;
import toothpick.registries.MemberInjectorRegistry;

/**
 * Generates a Registry for a given {@link RegistryInjectionTarget}.
 *
 * See {@link FactoryRegistry} and {@link MemberInjectorRegistry} for Registry types.
 */
public class RegistryGenerator extends CodeGenerator {

  /* @VisibleForTesting */ static int groupSize = 200;

  private static final String MAP_FIELD_NAME = "classNameToIndex";
  private static final String GET_FROM_THIS_REGISTRY_METHOD_NAME = "getFromThisRegistry";
  private final RegistryInjectionTarget registryInjectionTarget;

  private final TypeVariableName typeVariable = TypeVariableName.get("T");
  private final ParameterizedTypeName factoryType;
  private final ParameterizedTypeName clazzArgType;
  private final int numGroups;

  private final List<String> classNameList = new ArrayList<>();

  public RegistryGenerator(RegistryInjectionTarget registryInjectionTarget, Types types) {
    super(types);
    this.registryInjectionTarget = registryInjectionTarget;
    factoryType = ParameterizedTypeName.get(ClassName.get(registryInjectionTarget.type), typeVariable);
    clazzArgType = ParameterizedTypeName.get(ClassName.get(Class.class), typeVariable);

    for (TypeElement element : registryInjectionTarget.injectionTargetList) {
      classNameList.add(getGeneratedFQNClassName(element));
    }

    numGroups = (classNameList.size() + groupSize - 1) / groupSize;
  }

  @Override
  public String brewJava() {
    TypeSpec.Builder classBuilder = TypeSpec.classBuilder(registryInjectionTarget.registryName)
        .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
        .superclass(ClassName.get(registryInjectionTarget.superClass));

    emitMapField(classBuilder);
    emitConstructor(classBuilder);
    emitRegisterMethods(classBuilder);
    emitPublicGetterMethod(classBuilder);
    emitGetFromThisRegistryMethod(classBuilder);
    emitGetFromGroupMethods(classBuilder);

    JavaFile javaFile = JavaFile.builder(registryInjectionTarget.packageName, classBuilder.build())
        .addFileComment("Generated code from Toothpick. Do not modify!")
        .build();

    return javaFile.toString();
  }

  private void emitMapField(TypeSpec.Builder classBuilder) {
    FieldSpec fieldSpec = FieldSpec.builder(ParameterizedTypeName.get(Map.class, String.class, Integer.class),
                              MAP_FIELD_NAME,
                              Modifier.PRIVATE, Modifier.FINAL)
                              .initializer("new $T<>()", HashMap.class)
                              .build();
    classBuilder.addField(fieldSpec);
  }

  private void emitConstructor(TypeSpec.Builder classBuilder) {
    MethodSpec.Builder constructorBuilder = MethodSpec.constructorBuilder().addModifiers(Modifier.PUBLIC);

    for (String childPackageName : registryInjectionTarget.childrenRegistryPackageNameList) {
      ClassName registryClassName = ClassName.get(childPackageName, registryInjectionTarget.registryName);
      constructorBuilder.addStatement("addChildRegistry(new $L())", registryClassName);
    }

    for (int groupIndex = 0; groupIndex < numGroups; groupIndex++) {
      constructorBuilder.addStatement("$L()", nameOfRegisterGroupMethod(groupIndex));
    }

    classBuilder.addMethod(constructorBuilder.build());
  }

  private void emitRegisterMethods(TypeSpec.Builder classBuilder) {
    for (int groupIndex = 0; groupIndex < numGroups; groupIndex++) {
      emitRegisterGroupMethod(classBuilder, groupIndex);
    }
  }

  private void emitRegisterGroupMethod(TypeSpec.Builder classBuilder, int groupIndex) {
    MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder(nameOfRegisterGroupMethod(groupIndex))
            .addModifiers(Modifier.PRIVATE);

    int groupStartIndex = groupIndex * groupSize;
    for (int indexInGroup = 0; indexInGroup < groupSize; indexInGroup++) {
      int index = groupStartIndex + indexInGroup;
      if (index >= classNameList.size()) {
        break;
      }
      methodBuilder.addStatement("$L.put($S, $L)",
              MAP_FIELD_NAME, classNameList.get(index), index);
    }
    classBuilder.addMethod(methodBuilder.build());
  }

  private void emitPublicGetterMethod(TypeSpec.Builder classBuilder) {
    MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder(registryInjectionTarget.getterName)
        .addTypeVariable(typeVariable)
        .addAnnotation(Override.class)
        .addModifiers(Modifier.PUBLIC)
        .addParameter(clazzArgType, "clazz")
        .returns(factoryType);

    methodBuilder.addStatement("$T factory = $L(clazz)", factoryType, GET_FROM_THIS_REGISTRY_METHOD_NAME);

    CodeBlock.Builder blockBuilder = CodeBlock.builder().beginControlFlow("if (factory == null)");
    blockBuilder.addStatement("return $L(clazz)", registryInjectionTarget.childrenGetterName);
    blockBuilder.endControlFlow();
    methodBuilder.addCode(blockBuilder.build());

    methodBuilder.addStatement("return factory");

    classBuilder.addMethod(methodBuilder.build());
  }

  private void emitGetFromThisRegistryMethod(TypeSpec.Builder classBuilder) {
    MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder(GET_FROM_THIS_REGISTRY_METHOD_NAME)
            .addTypeVariable(typeVariable)
            .addModifiers(Modifier.PRIVATE)
            .addParameter(clazzArgType, "clazz")
            .returns(factoryType);

    if (classNameList.isEmpty()) {
      methodBuilder.addStatement("return null");
      classBuilder.addMethod(methodBuilder.build());
      return;
    }

    methodBuilder.addStatement("$T index = $L.get(clazz.getName())", Integer.class, MAP_FIELD_NAME);

    CodeBlock.Builder blockBuilder = CodeBlock.builder().beginControlFlow("if (index == null)");
    blockBuilder.addStatement("return null");
    blockBuilder.endControlFlow();
    methodBuilder.addCode(blockBuilder.build());

    methodBuilder.addStatement("int groupIndex = index / $L", groupSize);

    CodeBlock.Builder switchBuilder = CodeBlock.builder().beginControlFlow("switch(groupIndex)");
    for (int groupIndex = 0; groupIndex < numGroups; groupIndex++) {
      switchBuilder.addStatement("case $L: return $L(index)",
              groupIndex, nameOfGetFromGroupMethod(groupIndex));
    }
    switchBuilder.endControlFlow();
    methodBuilder.addCode(switchBuilder.build());

    methodBuilder.addStatement("return null");

    classBuilder.addMethod(methodBuilder.build());
  }

  private void emitGetFromGroupMethods(TypeSpec.Builder classBuilder) {
    for (int groupIndex = 0; groupIndex < numGroups; groupIndex++) {
      emitGetFromGroupMethod(classBuilder, groupIndex);
    }
  }

  private void emitGetFromGroupMethod(TypeSpec.Builder classBuilder, int groupIndex) {
    MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder(nameOfGetFromGroupMethod(groupIndex))
            .addTypeVariable(typeVariable)
            .addModifiers(Modifier.PRIVATE)
            .addParameter(TypeName.INT, "index")
            .returns(factoryType);

    int groupStartIndex = groupIndex * groupSize;
    String typeSimpleName = registryInjectionTarget.type.getSimpleName();
    CodeBlock.Builder switchBuilder = CodeBlock.builder().beginControlFlow("switch(index)");
    for (int index = groupStartIndex;
         index < groupStartIndex + groupSize && index < classNameList.size(); index++) {
      switchBuilder.addStatement("case $L: return ($L<T>) new $L$$$$$L()",
              index, typeSimpleName, classNameList.get(index), typeSimpleName);
    }
    switchBuilder.endControlFlow();
    methodBuilder.addCode(switchBuilder.build());
    methodBuilder.addStatement("return null");

    classBuilder.addMethod(methodBuilder.build());
  }

  private String nameOfRegisterGroupMethod(int groupIndex) {
    return "registerGroup" + groupIndex;
  }

  private String nameOfGetFromGroupMethod(int groupIndex) {
    return "getFromGroup" + groupIndex;
  }

  @Override
  public String getFqcn() {
    return registryInjectionTarget.getFqcn();
  }
}
