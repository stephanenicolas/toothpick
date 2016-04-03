package toothpick.compiler.factory;

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
import toothpick.compiler.CodeGenerator;

public class FactoryGenerator implements CodeGenerator {

  private static final String FACTORY_SUFFIX = "$$Factory";

  private FactoryInjectionTarget factoryInjectionTarget;

  public FactoryGenerator(FactoryInjectionTarget factoryInjectionTarget) {
    this.factoryInjectionTarget = factoryInjectionTarget;
  }

  public String brewJava() {
    // Interface to implement
    ClassName className = ClassName.get(factoryInjectionTarget.classPackage, factoryInjectionTarget.className);
    ParameterizedTypeName parameterizedTypeName = ParameterizedTypeName.get(ClassName.get(Factory.class), className);

    // Build class
    TypeSpec.Builder factoryTypeSpec = TypeSpec.classBuilder(factoryInjectionTarget.className + FACTORY_SUFFIX)
        .addModifiers(Modifier.PUBLIC)
        .addSuperinterface(parameterizedTypeName);
    emitCreateInstance(factoryTypeSpec);
    emitHasSingleton(factoryTypeSpec);
    emitHasProducesSingleton(factoryTypeSpec);

    JavaFile javaFile = JavaFile.builder(factoryInjectionTarget.classPackage, factoryTypeSpec.build())
        .addFileComment("Generated code from ToothPick. Do not modify!")
        .build();
    return javaFile.toString();
  }

  public String getFqcn() {
    return factoryInjectionTarget.getFqcn() + FACTORY_SUFFIX;
  }

  private void emitCreateInstance(TypeSpec.Builder builder) {
    MethodSpec.Builder createInstanceBuilder = MethodSpec.methodBuilder("createInstance")
        .addAnnotation(Override.class)
        .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
        .addParameter(ClassName.get(Injector.class), "injector")
        .returns(ClassName.get(factoryInjectionTarget.classPackage, factoryInjectionTarget.className));

    StringBuilder localVarStatement = new StringBuilder("");
    localVarStatement.append(factoryInjectionTarget.className).append(" ");
    String varName = "" + Character.toLowerCase(factoryInjectionTarget.className.charAt(0));
    varName += factoryInjectionTarget.className.substring(1);
    localVarStatement.append(varName).append(" = ");
    localVarStatement.append("new ");
    localVarStatement.append(factoryInjectionTarget.className).append("(");
    int counter = 1;
    String prefix = "";

    for (TypeMirror typeMirror : factoryInjectionTarget.parameters) {
      String paramName = "param" + counter++;
      TypeName paramType = TypeName.get(typeMirror);
      createInstanceBuilder.addStatement("$T $L = injector.getInstance($T.class)", paramType, paramName, paramType);
      localVarStatement.append(prefix);
      localVarStatement.append(paramName);
      prefix = ", ";
    }

    localVarStatement.append(")");
    createInstanceBuilder.addStatement(localVarStatement.toString());
    if (factoryInjectionTarget.needsMemberInjection) {
      StringBuilder injectStatement = new StringBuilder("injector.inject(");
      injectStatement.append(varName);
      injectStatement.append(")");
      createInstanceBuilder.addStatement(injectStatement.toString());
    }
    StringBuilder returnStatement = new StringBuilder("return ");
    returnStatement.append(varName);
    createInstanceBuilder.addStatement(returnStatement.toString());

    builder.addMethod(createInstanceBuilder.build());
  }

  private void emitHasSingleton(TypeSpec.Builder builder) {
    MethodSpec.Builder hasSingletonBuilder = MethodSpec.methodBuilder("hasSingletonAnnotation")
        .addAnnotation(Override.class)
        .addModifiers(Modifier.PUBLIC)
        .returns(TypeName.BOOLEAN)
        .addStatement("return $L", factoryInjectionTarget.hasSingletonAnnotation);
    builder.addMethod(hasSingletonBuilder.build());
  }

  private void emitHasProducesSingleton(TypeSpec.Builder builder) {
    MethodSpec.Builder hasProducesSingletonBuilder = MethodSpec.methodBuilder("hasProducesSingletonAnnotation")
        .addAnnotation(Override.class)
        .addModifiers(Modifier.PUBLIC)
        .returns(TypeName.BOOLEAN)
        .addStatement("return $L", factoryInjectionTarget.hasProducesSingletonAnnotation);
    builder.addMethod(hasProducesSingletonBuilder.build());
  }
}
