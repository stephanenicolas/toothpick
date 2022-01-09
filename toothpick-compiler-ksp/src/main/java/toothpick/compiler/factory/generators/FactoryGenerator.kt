/*
 * Copyright 2019 Stephane Nicolas
 * Copyright 2019 Daniel Molinero Reguera
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package toothpick.compiler.factory.generators

import com.squareup.javapoet.*
import toothpick.Factory
import toothpick.MemberInjector
import toothpick.Scope
import toothpick.compiler.common.generators.CodeGenerator
import toothpick.compiler.factory.targets.ConstructorInjectionTarget
import javax.inject.Singleton
import javax.lang.model.element.Modifier
import javax.lang.model.util.Types

/**
 * Generates a [Factory] for a given [ConstructorInjectionTarget]. Typically a factory
 * is created for a class a soon as it contains an [javax.inject.Inject] annotated
 * constructor. See Optimistic creation of factories in TP wiki.
 */
class FactoryGenerator(
    private val constructorInjectionTarget: ConstructorInjectionTarget,
    types: Types
) : CodeGenerator(types) {

    override fun brewJava(): String {
        // Interface to implement
        val className = ClassName.get(constructorInjectionTarget.builtClass)
        val parameterizedTypeName = ParameterizedTypeName.get(ClassName.get(Factory::class.java), className)
        val factoryClassName = getGeneratedSimpleClassName(constructorInjectionTarget.builtClass) + FACTORY_SUFFIX

        // Build class
        val factoryTypeSpec = TypeSpec.classBuilder(factoryClassName)
            .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
            .addSuperinterface(parameterizedTypeName)
            .emitSuperMemberInjectorFieldIfNeeded()
            .emitCreateInstance()
            .emitGetTargetScope()
            .emitHasScopeAnnotation()
            .emitHasSingletonAnnotation()
            .emitHasReleasableAnnotation()
            .emitHasProvidesSingletonAnnotation()
            .emitHasProvidesReleasableAnnotation()
            .build()

        return JavaFile.builder(className.packageName(), factoryTypeSpec)
            .build()
            .toString()
    }

    private fun TypeSpec.Builder.emitSuperMemberInjectorFieldIfNeeded(): TypeSpec.Builder = apply {
        if (constructorInjectionTarget.superClassThatNeedsMemberInjection != null) {
            val superTypeThatNeedsInjection =
                ClassName.get(constructorInjectionTarget.superClassThatNeedsMemberInjection)

            val memberInjectorSuperParameterizedTypeName =
                ParameterizedTypeName.get(
                    ClassName.get(MemberInjector::class.java),
                    superTypeThatNeedsInjection
                )

            // TODO use proper typing here
            val superMemberInjectorField =
                FieldSpec.builder(
                    memberInjectorSuperParameterizedTypeName,
                    "memberInjector",
                    Modifier.PRIVATE
                ).initializer(
                    "new \$L__MemberInjector()",
                    getGeneratedFQNClassName(
                        constructorInjectionTarget.superClassThatNeedsMemberInjection
                    )
                )

            addField(superMemberInjectorField.build())
        }
    }

    override val fqcn: String
        get() = getGeneratedFQNClassName(constructorInjectionTarget.builtClass) + FACTORY_SUFFIX

    private fun TypeSpec.Builder.emitCreateInstance(): TypeSpec.Builder = apply {
        val className = ClassName.get(constructorInjectionTarget.builtClass)
        val createInstanceBuilder =
            MethodSpec.methodBuilder("createInstance")
                .addAnnotation(Override::class.java)
                .addModifiers(Modifier.PUBLIC)
                .addParameter(ClassName.get(Scope::class.java), "scope")
                .returns(className)

        // change the scope to target scope so that all dependencies are created in the target scope
        // and the potential injection take place in the target scope too
        if (constructorInjectionTarget.parameters.isNotEmpty()
            || constructorInjectionTarget.superClassThatNeedsMemberInjection != null
        ) {
            // We only need it when the constructor contains parameters or dependencies
            createInstanceBuilder.addStatement("scope = getTargetScope(scope)")
        }

        val localVarStatement = StringBuilder("")
        val simpleClassName = getSimpleClassName(className)
        localVarStatement.append(simpleClassName).append(" ")
        var varName = "" + className.simpleName()[0].lowercaseChar()
        varName += className.simpleName().substring(1)
        localVarStatement.append(varName).append(" = ")
        localVarStatement.append("new ")
        localVarStatement.append(simpleClassName).append("(")
        var counter = 1
        var prefix = ""
        val codeBlockBuilder = CodeBlock.builder()
        if (constructorInjectionTarget.throwsThrowable) {
            codeBlockBuilder.beginControlFlow("try")
        }
        for (paramInjectionTarget in constructorInjectionTarget.parameters) {
            val invokeScopeGetMethodWithNameCodeBlock = getInvokeScopeGetMethodWithNameCodeBlock(paramInjectionTarget)
            val paramName = "param" + counter++
            codeBlockBuilder.add("\$T \$L = scope.", getParamType(paramInjectionTarget), paramName)
            codeBlockBuilder.add(invokeScopeGetMethodWithNameCodeBlock)
            codeBlockBuilder.add(";")
            codeBlockBuilder.add(LINE_SEPARATOR)
            localVarStatement.append(prefix)
            localVarStatement.append(paramName)
            prefix = ", "
        }
        localVarStatement.append(")")
        codeBlockBuilder.addStatement(localVarStatement.toString())
        if (constructorInjectionTarget.superClassThatNeedsMemberInjection != null) {
            codeBlockBuilder.addStatement("memberInjector.inject(\$L, scope)", varName)
        }
        codeBlockBuilder.addStatement("return \$L", varName)
        if (constructorInjectionTarget.throwsThrowable) {
            codeBlockBuilder.nextControlFlow("catch(\$L ex)", ClassName.get(Throwable::class.java))
            codeBlockBuilder.addStatement("throw new \$L(ex)", ClassName.get(RuntimeException::class.java))
            codeBlockBuilder.endControlFlow()
        }
        createInstanceBuilder.addCode(codeBlockBuilder.build())

        addMethod(createInstanceBuilder.build())
    }

    private fun TypeSpec.Builder.emitGetTargetScope(): TypeSpec.Builder = apply {
        addMethod(
            MethodSpec.methodBuilder("getTargetScope")
                .addAnnotation(Override::class.java)
                .addModifiers(Modifier.PUBLIC)
                .addParameter(ClassName.get(Scope::class.java), "scope")
                .returns(ClassName.get(Scope::class.java))
                .addStatement("return scope\$L", parentScopeCodeBlockBuilder.build().toString())
                .build()
        )
    }

    private fun TypeSpec.Builder.emitHasScopeAnnotation(): TypeSpec.Builder = apply {
        val scopeName = constructorInjectionTarget.scopeName
        val hasScopeAnnotation = scopeName != null
        addMethod(
            MethodSpec.methodBuilder("hasScopeAnnotation")
                .addAnnotation(Override::class.java)
                .addModifiers(Modifier.PUBLIC)
                .returns(TypeName.BOOLEAN)
                .addStatement("return \$L", hasScopeAnnotation)
                .build()
        )
    }

    private fun TypeSpec.Builder.emitHasSingletonAnnotation(): TypeSpec.Builder = apply {
        addMethod(
            MethodSpec.methodBuilder("hasSingletonAnnotation")
                .addAnnotation(Override::class.java)
                .addModifiers(Modifier.PUBLIC)
                .returns(TypeName.BOOLEAN)
                .addStatement("return \$L", constructorInjectionTarget.hasSingletonAnnotation)
                .build()
        )
    }

    private fun TypeSpec.Builder.emitHasReleasableAnnotation(): TypeSpec.Builder {
        addMethod(
            MethodSpec.methodBuilder("hasReleasableAnnotation")
                .addAnnotation(Override::class.java)
                .addModifiers(Modifier.PUBLIC)
                .returns(TypeName.BOOLEAN)
                .addStatement(
                    "return \$L",
                    constructorInjectionTarget.hasReleasableAnnotation
                )
                .build()
        )
        return this
    }

    private fun TypeSpec.Builder.emitHasProvidesSingletonAnnotation(): TypeSpec.Builder = apply {
        addMethod(
            MethodSpec.methodBuilder("hasProvidesSingletonAnnotation")
                .addAnnotation(Override::class.java)
                .addModifiers(Modifier.PUBLIC)
                .returns(TypeName.BOOLEAN)
                .addStatement(
                    "return \$L",
                    constructorInjectionTarget.hasProvidesSingletonInScopeAnnotation
                )
                .build()
        )
    }

    private fun TypeSpec.Builder.emitHasProvidesReleasableAnnotation(): TypeSpec.Builder = apply {
        addMethod(
            MethodSpec.methodBuilder("hasProvidesReleasableAnnotation")
                .addAnnotation(Override::class.java)
                .addModifiers(Modifier.PUBLIC)
                .returns(TypeName.BOOLEAN)
                .addStatement(
                    "return \$L",
                    constructorInjectionTarget.hasProvidesReleasableAnnotation
                )
                .build()
        )
    }

    // there is no scope name or the current @Scoped annotation.
    private val parentScopeCodeBlockBuilder: CodeBlock.Builder
        get() {
            val getParentScopeCodeBlockBuilder = CodeBlock.builder()
            val scopeName = constructorInjectionTarget.scopeName
            if (scopeName != null) {
                // there is no scope name or the current @Scoped annotation.
                if (Singleton::class.java.name == scopeName) {
                    getParentScopeCodeBlockBuilder.add(".getRootScope()")
                } else {
                    getParentScopeCodeBlockBuilder.add(".getParentScope(\$L.class)", scopeName)
                }
            }
            return getParentScopeCodeBlockBuilder
        }

    companion object {
        private const val FACTORY_SUFFIX = "__Factory"
    }
}