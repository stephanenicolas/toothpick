package toothpick.compiler;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import javax.lang.model.element.Modifier;
import javax.lang.model.type.TypeMirror;
import toothpick.Factory;
import toothpick.Injector;

public class FactoryGenerator {

  private static final String FACTORY_SUFFIX = "$$Factory";

  private FactoryInjectionTarget factoryInjectionTarget;

  public FactoryGenerator(FactoryInjectionTarget factoryInjectionTarget) {
    this.factoryInjectionTarget = factoryInjectionTarget;
  }

  public String brewJava() {
    // Interface to implement
    ClassName className =
        ClassName.get(factoryInjectionTarget.classPackage, factoryInjectionTarget.className);
    ParameterizedTypeName parameterizedTypeName =
        ParameterizedTypeName.get(className, ClassName.get(Factory.class));

    // Build class
    TypeSpec.Builder factoryTypeSpec =
        TypeSpec.classBuilder(factoryInjectionTarget.className + FACTORY_SUFFIX)
            .addModifiers(Modifier.PUBLIC)
            .addSuperinterface(parameterizedTypeName);
    emitCreateInstance(factoryTypeSpec);
    emitHasSingleton(factoryTypeSpec);
    emitHasProducesSingleton(factoryTypeSpec);

    JavaFile javaFile =
        JavaFile.builder(factoryInjectionTarget.classPackage, factoryTypeSpec.build())
            .addFileComment("Generated code from ToothPick. Do not modify!")
            .build();
    return javaFile.toString();
  }

  public String getFqcn() {
    return factoryInjectionTarget.getFqcn() + FACTORY_SUFFIX;
  }

  private void emitCreateInstance(TypeSpec.Builder builder) {
    MethodSpec.Builder createInstanceBuilder = MethodSpec.methodBuilder("createInstance")
        .addParameter(ClassName.get(Injector.class), "injector")
        .returns(
            ClassName.get(factoryInjectionTarget.classPackage, factoryInjectionTarget.className));

    StringBuilder returnStatement = new StringBuilder("return new ");
    returnStatement.append(factoryInjectionTarget.className).append("(");
    int counter = 1;

    for (TypeMirror typeMirror : factoryInjectionTarget.parameters) {
      String paramName = String.format("param$d", counter);
      TypeName paramType = TypeName.get(typeMirror);
      createInstanceBuilder.addStatement("$T $s = injector.getInstance($T.class)", paramType,
          paramName, paramType);
      returnStatement.append(paramName);
    }

    returnStatement.append(")");
    createInstanceBuilder.addStatement(returnStatement.toString());

    builder.addMethod(createInstanceBuilder.build());
  }

  private void emitHasSingleton(TypeSpec.Builder builder) {
    MethodSpec.Builder hasSingletonBuilder = MethodSpec.methodBuilder("hasSingletonAnnotation")
        .returns(TypeName.BOOLEAN)
        .addStatement("return $b", factoryInjectionTarget.hasSingletonAnnotation);
    builder.addMethod(hasSingletonBuilder.build());
  }

  private void emitHasProducesSingleton(TypeSpec.Builder builder) {
    MethodSpec.Builder hasProducesSingletonBuilder =
        MethodSpec.methodBuilder("hasProducesSingletonAnnotation")
            .returns(TypeName.BOOLEAN)
            .addStatement("return $b", factoryInjectionTarget.hasProducesSingletonAnnotation);
    builder.addMethod(hasProducesSingletonBuilder.build());
  }
}
