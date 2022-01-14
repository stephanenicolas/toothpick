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
package toothpick.compiler.memberinjector

import org.jetbrains.annotations.TestOnly
import toothpick.compiler.common.ToothpickProcessor
import toothpick.compiler.common.ToothpickProcessorOptions
import toothpick.compiler.common.generators.fields
import toothpick.compiler.common.generators.methods
import toothpick.compiler.memberinjector.generators.MemberInjectorGenerator
import toothpick.compiler.memberinjector.targets.FieldInjectionTarget
import toothpick.compiler.memberinjector.targets.MethodInjectionTarget
import javax.annotation.processing.RoundEnvironment
import javax.annotation.processing.SupportedAnnotationTypes
import javax.annotation.processing.SupportedOptions
import javax.inject.Inject
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.TypeElement

/**
 * This processor's role is to create [MemberInjector]. We create factories in different
 * situations :
 *
 *
 *  * When a class `Foo` has an [javax.inject.Singleton] annotated field : <br></br>
 * --> we create a MemberInjector to inject `Foo` instances.
 *  * When a class `Foo` has an [javax.inject.Singleton] method : <br></br>
 * --> we create a MemberInjector to inject `Foo` instances.
 *
 */
// http://stackoverflow.com/a/2067863/693752
@SupportedAnnotationTypes(ToothpickProcessor.INJECT_ANNOTATION_CLASS_NAME)
@SupportedOptions(
    ToothpickProcessor.PARAMETER_EXCLUDES,
    ToothpickProcessor.PARAMETER_CRASH_WHEN_INJECTED_METHOD_IS_NOT_PACKAGE
)
open class MemberInjectorProcessor : ToothpickProcessor() {

    private val allRoundsGeneratedToTypeElement = mutableMapOf<String, TypeElement>()

    override fun process(annotations: Set<TypeElement>, roundEnv: RoundEnvironment): Boolean {
        val injectedElements = roundEnv.getElementsAnnotatedWith(Inject::class.java)

        val typeElementToFieldInjectorTargetList: Map<TypeElement, List<FieldInjectionTarget>> =
            injectedElements.fields
                .filterNot { field -> (field.enclosingElement as TypeElement).isExcludedByFilters() }
                .filter { field -> field.isValidInjectAnnotatedFieldOrParameter() }
                .groupBy { field -> field.enclosingElement as TypeElement }
                .asSequence()
                .associate { (enclosing, fields) ->
                    enclosing to fields.map { field -> field.createFieldOrParamInjectionTarget() }
                }

        val typeElementToMethodInjectorTargetList: Map<TypeElement, List<MethodInjectionTarget>> =
            injectedElements.methods
                .filterNot { method -> (method.enclosingElement as TypeElement).isExcludedByFilters() }
                .filter { method -> method.isValidInjectAnnotatedMethod() }
                .groupBy { method -> method.enclosingElement as TypeElement }
                .asSequence()
                .associate { (enclosing, methods) ->
                    enclosing to methods.map { method -> method.createMethodInjectionTarget() }
                }

        val elementWithInjectionSet: Set<TypeElement> =
            typeElementToFieldInjectorTargetList.keys + typeElementToMethodInjectorTargetList.keys

        val typeElementToSuperTypeElementThatNeedsInjection: Map<TypeElement, TypeElement?> =
            elementWithInjectionSet.associateWith { enclosing ->
                enclosing.getMostDirectSuperClassWithInjectedMembers(onlyParents = true)
            }

        // Generate member scopes
        elementWithInjectionSet
            .map { typeElement ->
                MemberInjectorGenerator(
                    targetClass = typeElement,
                    superClassThatNeedsInjection = typeElementToSuperTypeElementThatNeedsInjection[typeElement],
                    fieldInjectionTargetList = typeElementToFieldInjectorTargetList[typeElement],
                    methodInjectionTargetList = typeElementToMethodInjectorTargetList[typeElement],
                    typeUtil = typeUtils
                )
            }
            .forEach { memberInjectorGenerator ->
                writeToFile(
                    codeGenerator = memberInjectorGenerator,
                    fileDescription = "MemberInjector for type ${memberInjectorGenerator.targetClass}"
                )

                allRoundsGeneratedToTypeElement[memberInjectorGenerator.fqcn] = memberInjectorGenerator.targetClass
            }

        return false
    }

    private fun ExecutableElement.createMethodInjectionTarget(): MethodInjectionTarget {
        val enclosingElement = enclosingElement as TypeElement
        return MethodInjectionTarget(
            enclosingClass = enclosingElement,
            methodName = simpleName.toString(),
            isOverride = enclosingElement.isOverride(methodElement = this),
            parameters = getParamInjectionTargetList(),
            exceptionTypes = getExceptionTypes()
        )
    }

    @TestOnly
    internal fun setCrashOrWarnWhenMethodIsNotPackageVisible(crashOrWarnWhenMethodIsNotPackageVisible: Boolean) {
        val current = optionsOverride ?: ToothpickProcessorOptions()
        optionsOverride = current.copy(
            crashWhenInjectedMethodIsNotPackageVisible = crashOrWarnWhenMethodIsNotPackageVisible
        )
    }

    @TestOnly
    internal fun getOriginatingElement(generatedQualifiedName: String): TypeElement? =
        allRoundsGeneratedToTypeElement[generatedQualifiedName]
}
