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
package toothpick.compiler.common.generators

import com.squareup.javapoet.ClassName
import com.squareup.javapoet.CodeBlock
import com.squareup.javapoet.ParameterizedTypeName
import com.squareup.javapoet.TypeName
import toothpick.compiler.common.generators.targets.ParamInjectionTarget
import javax.lang.model.element.ElementKind
import javax.lang.model.element.TypeElement
import javax.lang.model.util.Types

/** Common base interface for all code generators.  */
abstract class CodeGenerator(protected val typeUtil: Types) {

    /**
     * Creates all java code.
     *
     * @return a string containing the java code generated by this [CodeGenerator].
     */
    abstract fun brewJava(): String

    protected fun getInvokeScopeGetMethodWithNameCodeBlock(
        paramInjectionTarget: ParamInjectionTarget
    ): CodeBlock {
        val injectionName = if (paramInjectionTarget.name == null) ""
        else ", \"${paramInjectionTarget.name}\""

        checkNotNull(paramInjectionTarget.kind) { "The kind can't be null." }

        val scopeGetMethodName: String = when (paramInjectionTarget.kind) {
            ParamInjectionTarget.Kind.INSTANCE -> "getInstance"
            ParamInjectionTarget.Kind.PROVIDER -> "getProvider"
            ParamInjectionTarget.Kind.LAZY -> "getLazy"
        }

        val className: ClassName = when (paramInjectionTarget.kind) {
            ParamInjectionTarget.Kind.INSTANCE -> ClassName.get(paramInjectionTarget.memberClass)
            ParamInjectionTarget.Kind.PROVIDER -> ClassName.get(paramInjectionTarget.kindParamClass)
            ParamInjectionTarget.Kind.LAZY -> ClassName.get(paramInjectionTarget.kindParamClass)
        }

        return CodeBlock.builder()
            .add("\$L(\$T.class\$L)", scopeGetMethodName, className, injectionName)
            .build()
    }

    protected fun getParamType(paramInjectionTarget: ParamInjectionTarget): TypeName {
        return when (paramInjectionTarget.kind) {
            ParamInjectionTarget.Kind.INSTANCE ->
                TypeName.get(typeUtil.erasure(paramInjectionTarget.memberClass.asType()))
            else -> {
                ParameterizedTypeName.get(
                    ClassName.get(paramInjectionTarget.memberClass),
                    ClassName.get(typeUtil.erasure(paramInjectionTarget.kindParamClass.asType()))
                )
            }
        }
    }

    /** @return the FQN of the code generated by this [CodeGenerator].
     */
    abstract val fqcn: String?

    companion object {

        @JvmStatic
        protected val LINE_SEPARATOR: String = System.getProperty("line.separator")

        @JvmStatic
        protected fun getGeneratedFQNClassName(typeElement: TypeElement): String =
            "${getGeneratedPackageName(typeElement)}.${getGeneratedSimpleClassName(typeElement)}"

        @JvmStatic
        protected fun getGeneratedSimpleClassName(typeElement: TypeElement): String {
            var currentTypeElement = typeElement
            var result = currentTypeElement.simpleName.toString()
            // deals with inner classes
            while (currentTypeElement.enclosingElement.kind != ElementKind.PACKAGE) {
                result = "${currentTypeElement.enclosingElement.simpleName}$$result"
                currentTypeElement = currentTypeElement.enclosingElement as TypeElement
            }
            return result
        }

        @JvmStatic
        protected fun getSimpleClassName(className: ClassName): String {
            return buildString {
                val simpleNames = className.simpleNames()
                for (i in simpleNames.indices) {
                    val name = simpleNames[i]
                    append(name)
                    if (i != simpleNames.size - 1) {
                        append(".")
                    }
                }
            }
        }

        protected fun getGeneratedPackageName(typeElement: TypeElement): String {
            // deals with inner classes
            var currentTypeElement = typeElement
            while (currentTypeElement.enclosingElement.kind != ElementKind.PACKAGE) {
                currentTypeElement = currentTypeElement.enclosingElement as TypeElement
            }
            return currentTypeElement.enclosingElement.toString()
        }
    }
}
