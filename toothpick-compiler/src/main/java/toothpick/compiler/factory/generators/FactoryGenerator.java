package toothpick.compiler.factory.generators;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import javax.lang.model.element.Modifier;
import javax.lang.model.util.Types;
import toothpick.Factory;
import toothpick.MemberInjector;
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

  public FactoryGenerator(ConstructorInjectionTarget constructorInjectionTarget, Types types) {
    super(types);
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
    emitSuperMemberInjectorFieldIfNeeded(factoryTypeSpec);
    emitCreateInstance(factoryTypeSpec);
    emitGetTargetScope(factoryTypeSpec);
    emitHasScopeAnnotation(factoryTypeSpec);
    emitHasScopeInstancesAnnotation(factoryTypeSpec);

    JavaFile javaFile = JavaFile.builder(className.packageName(), factoryTypeSpec.build()).build();
    return javaFile.toString();
  }

  private void emitSuperMemberInjectorFieldIfNeeded(TypeSpec.Builder scopeMemberTypeSpec) {
    if (constructorInjectionTarget.superClassThatNeedsMemberInjection != null) {
      ClassName superTypeThatNeedsInjection = ClassName.get(constructorInjectionTarget.superClassThatNeedsMemberInjection);
      ParameterizedTypeName memberInjectorSuperParameterizedTypeName =
          ParameterizedTypeName.get(ClassName.get(MemberInjector.class), superTypeThatNeedsInjection);
      FieldSpec.Builder superMemberInjectorField =
          FieldSpec.builder(memberInjectorSuperParameterizedTypeName, "memberInjector", Modifier.PRIVATE)
              //TODO use proper typing here
              .initializer("new $L$$$$MemberInjector()", getGeneratedFQNClassName(constructorInjectionTarget.superClassThatNeedsMemberInjection));
      scopeMemberTypeSpec.addField(superMemberInjectorField.build());
    }
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

    //change the scope to target scope so that all dependencies are created in the target scope
    //and the potential injection take place in the target scope too
    if (!constructorInjectionTarget.parameters.isEmpty()
        || constructorInjectionTarget.superClassThatNeedsMemberInjection != null) {
      // We only need it when the constructor contains parameters or dependencies
      createInstanceBuilder.addStatement("scope = getTargetScope(scope)");
    }

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

    CodeBlock.Builder codeBlockBuilder = CodeBlock.builder();
    if (constructorInjectionTarget.throwsThrowable) {
      codeBlockBuilder.beginControlFlow("try");
    }

    for (ParamInjectionTarget paramInjectionTarget : constructorInjectionTarget.parameters) {
      CodeBlock invokeScopeGetMethodWithNameCodeBlock = getInvokeScopeGetMethodWithNameCodeBlock(paramInjectionTarget);
      String paramName = "param" + counter++;
      codeBlockBuilder.add("$T $L = scope.", getParamType(paramInjectionTarget), paramName);
      codeBlockBuilder.add(invokeScopeGetMethodWithNameCodeBlock);
      codeBlockBuilder.add(";");
      codeBlockBuilder.add(LINE_SEPARATOR);
      localVarStatement.append(prefix);
      localVarStatement.append(paramName);
      prefix = ", ";
    }

    localVarStatement.append(")");
    codeBlockBuilder.addStatement(localVarStatement.toString());

    if (constructorInjectionTarget.superClassThatNeedsMemberInjection != null) {
      codeBlockBuilder.addStatement("memberInjector.inject($L, scope)", varName);
    }
    codeBlockBuilder.addStatement("return $L", varName);
    if (constructorInjectionTarget.throwsThrowable) {
      codeBlockBuilder.nextControlFlow("catch($L ex)", ClassName.get(Throwable.class));
      codeBlockBuilder.addStatement("throw new $L(ex)", ClassName.get(RuntimeException.class));
      codeBlockBuilder.endControlFlow();
    }
    createInstanceBuilder.addCode(codeBlockBuilder.build());

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
    MethodSpec.Builder hasProducesSingletonBuilder = MethodSpec.methodBuilder("hasProvidesSingletonInScopeAnnotation")
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
