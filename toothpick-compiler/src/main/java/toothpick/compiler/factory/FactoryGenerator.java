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
import toothpick.compiler.targets.ConstructorInjectionTarget;

/**
 * Generates a {@link Factory} for a given {@link ConstructorInjectionTarget}.
 * Typically a factory is created for a class a soon as it contains
 * an {@link javax.inject.Inject} annotated constructor.
 * TODO also generate a factory for a <em>non private & non abstract class</em>
 * whose instances are injected (i.e. the type of an injected field or of a parameter
 * of an injected constructor.
 */
public class FactoryGenerator implements CodeGenerator {

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
    TypeSpec.Builder factoryTypeSpec = TypeSpec.classBuilder(className.simpleName() + FACTORY_SUFFIX)
        .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
        .addSuperinterface(parameterizedTypeName);
    emitCreateInstance(factoryTypeSpec);
    emitHasSingleton(factoryTypeSpec);
    emitHasProducesSingleton(factoryTypeSpec);

    JavaFile javaFile = JavaFile.builder(className.packageName(), factoryTypeSpec.build()).build();
    return javaFile.toString();
  }

  @Override public String getFqcn() {
    return constructorInjectionTarget.builtClass.getQualifiedName().toString() + FACTORY_SUFFIX;
  }

  private void emitCreateInstance(TypeSpec.Builder builder) {
    ClassName className = ClassName.get(constructorInjectionTarget.builtClass);
    MethodSpec.Builder createInstanceBuilder = MethodSpec.methodBuilder("createInstance")
        .addAnnotation(Override.class)
        .addModifiers(Modifier.PUBLIC)
        .addParameter(ClassName.get(Injector.class), "injector")
        .returns(className);

    StringBuilder localVarStatement = new StringBuilder("");
    localVarStatement.append(className.simpleName()).append(" ");
    String varName = "" + Character.toLowerCase(className.simpleName().charAt(0));
    varName += className.simpleName().substring(1);
    localVarStatement.append(varName).append(" = ");
    localVarStatement.append("new ");
    localVarStatement.append(className.simpleName()).append("(");
    int counter = 1;
    String prefix = "";

    for (TypeMirror typeMirror : constructorInjectionTarget.parameters) {
      String paramName = "param" + counter++;
      TypeName paramType = TypeName.get(typeMirror);
      createInstanceBuilder.addStatement("$T $L = injector.getInstance($T.class)", paramType, paramName, paramType);
      localVarStatement.append(prefix);
      localVarStatement.append(paramName);
      prefix = ", ";
    }

    localVarStatement.append(")");
    createInstanceBuilder.addStatement(localVarStatement.toString());
    if (constructorInjectionTarget.needsMemberInjection) {
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
        .addStatement("return $L", constructorInjectionTarget.hasSingletonAnnotation);
    builder.addMethod(hasSingletonBuilder.build());
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
