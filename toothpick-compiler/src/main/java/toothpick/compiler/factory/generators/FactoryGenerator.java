package toothpick.compiler.factory.generators;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import javax.lang.model.element.Modifier;
import toothpick.Factory;
import toothpick.Scope;
import toothpick.compiler.common.generators.CodeGenerator;
import toothpick.compiler.common.generators.targets.ParamInjectionTarget;
import toothpick.compiler.factory.targets.ConstructorInjectionTarget;

/**
 * Generates a {@link Factory} for a given {@link ConstructorInjectionTarget}.
 * Typically a factory is created for a class a soon as it contains
 * an {@link javax.inject.Inject} annotated constructor.
 * See Optimistic creation of factories in TP wiki.
 */
public class FactoryGenerator extends CodeGenerator {

  private static final String FACTORY_SUFFIX = "$$Factory";

  private ConstructorInjectionTarget constructorInjectionTarget;

  public FactoryGenerator(ConstructorInjectionTarget constructorInjectionTarget) {
    this.constructorInjectionTarget = constructorInjectionTarget;
  }

  public String brewJava() {
    // Interface to implement
    ClassName className = ClassName.get(constructorInjectionTarget.builtClass);
    ParameterizedTypeName parameterizedTypeName = ParameterizedTypeName.get(ClassName.get(Factory.class), className);

    // Build class
    TypeSpec.Builder factoryTypeSpec = TypeSpec.classBuilder(getGeneratedSimpleClassName(constructorInjectionTarget.builtClass) + FACTORY_SUFFIX)
        .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
        .addSuperinterface(parameterizedTypeName);
    emitCreateInstance(factoryTypeSpec);
    emitGetTargetScope(factoryTypeSpec);
    emitHasScopeAnnotation(factoryTypeSpec);
    emitHasScopeInstancesAnnotation(factoryTypeSpec);

    JavaFile javaFile = JavaFile.builder(className.packageName(), factoryTypeSpec.build()).build();
    return javaFile.toString();
  }

  @Override
  public String getFqcn() {
    return getGeneratedFQNClassName(constructorInjectionTarget.builtClass) + FACTORY_SUFFIX;
  }

  private void emitCreateInstance(TypeSpec.Builder builder) {
    ClassName className = ClassName.get(constructorInjectionTarget.builtClass);
    MethodSpec.Builder createInstanceBuilder = MethodSpec.methodBuilder("createInstance")
        .addAnnotation(Override.class)
        .addModifiers(Modifier.PUBLIC)
        .addParameter(ClassName.get(Scope.class), "scope")
        .returns(className);

    StringBuilder localVarStatement = new StringBuilder("");
    String simpleClassName = getSimpleClassName(className);
    localVarStatement.append(simpleClassName).append(" ");
    String varName = "" + Character.toLowerCase(className.simpleName().charAt(0));
    varName += className.simpleName().substring(1);
    localVarStatement.append(varName).append(" = ");
    localVarStatement.append("new ");
    localVarStatement.append(simpleClassName).append("(");
    int counter = 1;
    String prefix = "";

    for (ParamInjectionTarget paramInjectionTarget : constructorInjectionTarget.parameters) {
      CodeBlock invokeScopeGetMethodWithNameCodeBlock = getInvokeScopeGetMethodWithNameCodeBlock(paramInjectionTarget);
      String paramName = "param" + counter++;
      TypeName paramType = TypeName.get(paramInjectionTarget.memberClass.asType());
      createInstanceBuilder.addCode("$T $L = scope.", paramType, paramName);
      createInstanceBuilder.addCode(invokeScopeGetMethodWithNameCodeBlock);
      createInstanceBuilder.addCode(";\n");
      localVarStatement.append(prefix);
      localVarStatement.append(paramName);
      prefix = ", ";
    }

    localVarStatement.append(")");
    createInstanceBuilder.addStatement(localVarStatement.toString());
    if (constructorInjectionTarget.superClassThatNeedsMemberInjection != null) {
      createInstanceBuilder.addStatement("new $L$$$$MemberInjector().inject($L, getTargetScope(scope))",
          getGeneratedFQNClassName(constructorInjectionTarget.superClassThatNeedsMemberInjection), varName);
    }
    createInstanceBuilder.addStatement("return $L", varName);

    builder.addMethod(createInstanceBuilder.build());
  }

  private void emitGetTargetScope(TypeSpec.Builder builder) {
    CodeBlock.Builder getParentScopeCodeBlockBuilder = getParentScopeCodeBlockBuilder();
    MethodSpec.Builder getScopeBuilder = MethodSpec.methodBuilder("getTargetScope")
        .addAnnotation(Override.class)
        .addModifiers(Modifier.PUBLIC)
        .addParameter(ClassName.get(Scope.class), "scope")
        .returns(ClassName.get(Scope.class))
        .addStatement("return scope$L", getParentScopeCodeBlockBuilder.build().toString());
    builder.addMethod(getScopeBuilder.build());
  }

  private void emitHasScopeAnnotation(TypeSpec.Builder builder) {
    String scopeName = constructorInjectionTarget.scopeName;
    boolean hasScopeAnnotation = scopeName != null;
    MethodSpec.Builder hasScopeAnnotationBuilder = MethodSpec.methodBuilder("hasScopeAnnotation")
        .addAnnotation(Override.class)
        .addModifiers(Modifier.PUBLIC)
        .returns(TypeName.BOOLEAN)
        .addStatement("return $L", hasScopeAnnotation);
    builder.addMethod(hasScopeAnnotationBuilder.build());
  }

  private void emitHasScopeInstancesAnnotation(TypeSpec.Builder builder) {
    MethodSpec.Builder hasProducesSingletonBuilder = MethodSpec.methodBuilder("hasScopeInstancesAnnotation")
        .addAnnotation(Override.class)
        .addModifiers(Modifier.PUBLIC)
        .returns(TypeName.BOOLEAN)
        .addStatement("return $L", constructorInjectionTarget.hasScopeInstancesAnnotation);
    builder.addMethod(hasProducesSingletonBuilder.build());
  }

  private CodeBlock.Builder getParentScopeCodeBlockBuilder() {
    CodeBlock.Builder getParentScopeCodeBlockBuilder = CodeBlock.builder();
    String scopeName = constructorInjectionTarget.scopeName;
    if (scopeName != null) {
      //there is no scope name or the current @Scoped annotation.
      if (javax.inject.Singleton.class.getName().equals(scopeName)) {
        getParentScopeCodeBlockBuilder.add(".getRootScope()");
      } else {
        getParentScopeCodeBlockBuilder.add(".getParentScope($L.class)", scopeName);
      }
    }
    return getParentScopeCodeBlockBuilder;
  }
}
