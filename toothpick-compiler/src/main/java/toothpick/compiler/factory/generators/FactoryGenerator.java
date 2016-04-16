package toothpick.compiler.factory.generators;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import javax.lang.model.element.Modifier;
import javax.lang.model.type.TypeMirror;
import toothpick.Factory;
import toothpick.Scope;
import toothpick.compiler.CodeGenerator;
import toothpick.compiler.CodeGeneratorUtil;
import toothpick.compiler.factory.targets.FactoryInjectionTarget;

/**
 * Generates a {@link Factory} for a given {@link FactoryInjectionTarget}.
 * Typically a factory is created for a class a soon as it contains
 * an {@link javax.inject.Inject} annotated constructor.
 * See Optimistic creation of factories in TP wiki. TODO
 */
public class FactoryGenerator implements CodeGenerator {

  private static final String FACTORY_SUFFIX = "$$Factory";

  private FactoryInjectionTarget factoryInjectionTarget;

  public FactoryGenerator(FactoryInjectionTarget factoryInjectionTarget) {
    this.factoryInjectionTarget = factoryInjectionTarget;
  }

  public String brewJava() {
    // Interface to implement
    ClassName className = ClassName.get(factoryInjectionTarget.builtClass);
    ParameterizedTypeName parameterizedTypeName = ParameterizedTypeName.get(ClassName.get(Factory.class), className);

    // Build class
    TypeSpec.Builder factoryTypeSpec = TypeSpec.classBuilder(CodeGeneratorUtil.getGeneratedSimpleClassName(factoryInjectionTarget.builtClass) + FACTORY_SUFFIX)
        .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
        .addSuperinterface(parameterizedTypeName);
    emitCreateInstance(factoryTypeSpec);
    emitHasSingleton(factoryTypeSpec);
    emitHasProducesSingleton(factoryTypeSpec);

    JavaFile javaFile = JavaFile.builder(className.packageName(), factoryTypeSpec.build()).build();
    return javaFile.toString();
  }

  @Override
  public String getFqcn() {
    return CodeGeneratorUtil.getGeneratedFQNClassName(factoryInjectionTarget.builtClass) + FACTORY_SUFFIX;
  }

  private void emitCreateInstance(TypeSpec.Builder builder) {
    ClassName className = ClassName.get(factoryInjectionTarget.builtClass);
    MethodSpec.Builder createInstanceBuilder = MethodSpec.methodBuilder("createInstance")
        .addAnnotation(Override.class)
        .addModifiers(Modifier.PUBLIC)
        .addParameter(ClassName.get(Scope.class), "scope")
        .returns(className);

    StringBuilder localVarStatement = new StringBuilder("");
    String simpleClassName = CodeGeneratorUtil.getSimpleClassName(className);
    localVarStatement.append(simpleClassName).append(" ");
    String varName = "" + Character.toLowerCase(className.simpleName().charAt(0));
    varName += className.simpleName().substring(1);
    localVarStatement.append(varName).append(" = ");
    localVarStatement.append("new ");
    localVarStatement.append(simpleClassName).append("(");
    int counter = 1;
    String prefix = "";

    for (TypeMirror typeMirror : factoryInjectionTarget.parameters) {
      String paramName = "param" + counter++;
      TypeName paramType = TypeName.get(typeMirror);
      createInstanceBuilder.addStatement("$T $L = scope.getInstance($T.class)", paramType, paramName, paramType);
      localVarStatement.append(prefix);
      localVarStatement.append(paramName);
      prefix = ", ";
    }

    localVarStatement.append(")");
    createInstanceBuilder.addStatement(localVarStatement.toString());
    if (factoryInjectionTarget.needsMemberInjection) {
      createInstanceBuilder.addStatement("new $L$$$$MemberInjector().inject($L, scope)", CodeGeneratorUtil.getGeneratedFQNClassName(factoryInjectionTarget.builtClass), varName);
    }
    createInstanceBuilder.addStatement("return $L", varName);

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
