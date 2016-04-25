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
    emitGetScopeName(factoryTypeSpec);
    emitHasProducesSingleton(factoryTypeSpec);

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
      CodeBlock.Builder getParentScopeCodeBlockBuilder = CodeBlock.builder();
      String scopeName = constructorInjectionTarget.scopeName;
      if (scopeName != null) {
        if(javax.inject.Singleton.class.getName().equals(scopeName)) {
          getParentScopeCodeBlockBuilder.add(".getRootScope()");
        } else {
          getParentScopeCodeBlockBuilder.add(".getParentScope($S)", scopeName);
        }
      } else {
        getParentScopeCodeBlockBuilder.add("");
      }
      createInstanceBuilder.addStatement("new $L$$$$MemberInjector().inject($L, scope$L)",
          getGeneratedFQNClassName(constructorInjectionTarget.superClassThatNeedsMemberInjection),
          varName,
          getParentScopeCodeBlockBuilder.build().toString());
    }
    createInstanceBuilder.addStatement("return $L", varName);

    builder.addMethod(createInstanceBuilder.build());
  }

  private void emitGetScopeName(TypeSpec.Builder builder) {
    CodeBlock.Builder getParentScopeCodeBlockBuilder = CodeBlock.builder();
    String scopeName = constructorInjectionTarget.scopeName;
    if (scopeName != null) {
      getParentScopeCodeBlockBuilder.add("$S", scopeName);
    } else {
      getParentScopeCodeBlockBuilder.add("null");
    }
    MethodSpec.Builder getScopeBuilder = MethodSpec.methodBuilder("getScopeName")
        .addAnnotation(Override.class)
        .addModifiers(Modifier.PUBLIC)
        .returns(ClassName.get(String.class))
        .addStatement("return $L", getParentScopeCodeBlockBuilder.build().toString());
    builder.addMethod(getScopeBuilder.build());
  }

  private void emitHasProducesSingleton(TypeSpec.Builder builder) {
    MethodSpec.Builder hasProducesSingletonBuilder = MethodSpec.methodBuilder("hasProducesSingletonAnnotation")
        .addAnnotation(Override.class)
        .addModifiers(Modifier.PUBLIC)
        .returns(TypeName.BOOLEAN)
        .addStatement("return $L", constructorInjectionTarget.hasProducesSingletonAnnotation);
    builder.addMethod(hasProducesSingletonBuilder.build());
  }
}
