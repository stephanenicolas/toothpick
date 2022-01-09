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
import toothpick.compiler.memberinjector.generators.MemberInjectorGenerator
import toothpick.compiler.memberinjector.targets.FieldInjectionTarget
import toothpick.compiler.memberinjector.targets.MethodInjectionTarget
import javax.annotation.processing.RoundEnvironment
import javax.annotation.processing.SupportedAnnotationTypes
import javax.annotation.processing.SupportedOptions
import javax.inject.Inject
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.TypeElement
import javax.lang.model.element.VariableElement
import javax.lang.model.util.ElementFilter

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

    private var mapTypeElementToFieldInjectorTargetList =
        mutableMapOf<TypeElement, MutableList<FieldInjectionTarget>>()

    private var mapTypeElementToMethodInjectorTargetList =
        mutableMapOf<TypeElement, MutableList<MethodInjectionTarget>>()

    private var mapTypeElementToSuperTypeElementThatNeedsInjection =
        mutableMapOf<TypeElement, TypeElement?>()

    private val allRoundsGeneratedToTypeElement = mutableMapOf<String, TypeElement>()

    override fun process(annotations: Set<TypeElement>, roundEnv: RoundEnvironment): Boolean {
        mapTypeElementToFieldInjectorTargetList = mutableMapOf()
        mapTypeElementToMethodInjectorTargetList = mutableMapOf()
        mapTypeElementToSuperTypeElementThatNeedsInjection = mutableMapOf()

        findAndParseTargets(roundEnv)

        // Generate member scopes
        val elementWithInjectionSet: MutableSet<TypeElement> = HashSet()
        elementWithInjectionSet.addAll(mapTypeElementToFieldInjectorTargetList.keys)
        elementWithInjectionSet.addAll(mapTypeElementToMethodInjectorTargetList.keys)

        for (typeElement in elementWithInjectionSet) {
            val fieldInjectionTargetList: List<FieldInjectionTarget>? =
                mapTypeElementToFieldInjectorTargetList[typeElement]
            val methodInjectionTargetList: List<MethodInjectionTarget>? =
                mapTypeElementToMethodInjectorTargetList[typeElement]
            val superClassThatNeedsInjection = mapTypeElementToSuperTypeElementThatNeedsInjection[typeElement]
            val memberInjectorGenerator = MemberInjectorGenerator(
                typeElement,
                superClassThatNeedsInjection,
                fieldInjectionTargetList,
                methodInjectionTargetList,
                typeUtils
            )

            writeToFile(
                codeGenerator = memberInjectorGenerator,
                fileDescription = "MemberInjector for type %s".format(typeElement),
                originatingElement = typeElement
            )

            allRoundsGeneratedToTypeElement[memberInjectorGenerator.fqcn] = typeElement
        }
        return false
    }

    private fun findAndParseTargets(roundEnv: RoundEnvironment) {
        processInjectAnnotatedFields(roundEnv)
        processInjectAnnotatedMethods(roundEnv)
    }

    private fun processInjectAnnotatedFields(roundEnv: RoundEnvironment) {
        for (element in ElementFilter.fieldsIn(roundEnv.getElementsAnnotatedWith(Inject::class.java))) {
            if (!isExcludedByFilters(element.enclosingElement as TypeElement)) {
                processInjectAnnotatedField(element, mapTypeElementToFieldInjectorTargetList)
            }
        }
    }

    private fun processInjectAnnotatedMethods(roundEnv: RoundEnvironment) {
        for (element in ElementFilter.methodsIn(roundEnv.getElementsAnnotatedWith(Inject::class.java))) {
            if (!isExcludedByFilters(element.enclosingElement as TypeElement)) {
                processInjectAnnotatedMethod(element, mapTypeElementToMethodInjectorTargetList)
            }
        }
    }

    private fun processInjectAnnotatedField(
        fieldElement: VariableElement,
        mapTypeElementToMemberInjectorTargetList: MutableMap<TypeElement, MutableList<FieldInjectionTarget>>?
    ) {
        val enclosingElement = fieldElement.enclosingElement as TypeElement

        // Verify common generated code restrictions.
        if (!isValidInjectAnnotatedFieldOrParameter(fieldElement)) {
            return
        }
        var fieldInjectionTargetList = mapTypeElementToMemberInjectorTargetList!![enclosingElement]
        if (fieldInjectionTargetList == null) {
            fieldInjectionTargetList = ArrayList()
            mapTypeElementToMemberInjectorTargetList[enclosingElement] = fieldInjectionTargetList
        }
        mapTypeToMostDirectSuperTypeThatNeedsInjection(enclosingElement)
        fieldInjectionTargetList.add(createFieldOrParamInjectionTarget(fieldElement))
    }

    private fun processInjectAnnotatedMethod(
        methodElement: ExecutableElement,
        mapTypeElementToMemberInjectorTargetList: MutableMap<TypeElement, MutableList<MethodInjectionTarget>>?
    ) {
        val enclosingElement = methodElement.enclosingElement as TypeElement

        // Verify common generated code restrictions.
        if (!isValidInjectAnnotatedMethod(methodElement)) {
            return
        }
        var methodInjectionTargetList = mapTypeElementToMemberInjectorTargetList!![enclosingElement]
        if (methodInjectionTargetList == null) {
            methodInjectionTargetList = ArrayList()
            mapTypeElementToMemberInjectorTargetList[enclosingElement] = methodInjectionTargetList
        }
        mapTypeToMostDirectSuperTypeThatNeedsInjection(enclosingElement)
        methodInjectionTargetList.add(createMethodInjectionTarget(methodElement))
    }

    private fun mapTypeToMostDirectSuperTypeThatNeedsInjection(typeElement: TypeElement) {
        val superClassWithInjectedMembers = getMostDirectSuperClassWithInjectedMembers(typeElement, true)
        mapTypeElementToSuperTypeElementThatNeedsInjection[typeElement] = superClassWithInjectedMembers
    }

    private fun createMethodInjectionTarget(methodElement: ExecutableElement): MethodInjectionTarget {
        val enclosingElement = methodElement.enclosingElement as TypeElement
        val methodName = methodElement.simpleName.toString()
        val isOverride = isOverride(enclosingElement, methodElement)
        val methodInjectionTarget = MethodInjectionTarget(enclosingElement, methodName, isOverride)
        methodInjectionTarget.parameters.addAll(getParamInjectionTargetList(methodElement))
        methodInjectionTarget.exceptionTypes.addAll(getExceptionTypes(methodElement))
        return methodInjectionTarget
    }

    @TestOnly
    internal fun setCrashOrWarnWhenMethodIsNotPackageVisible(
        crashOrWarnWhenMethodIsNotPackageVisible: Boolean
    ) {
        val current = optionsOverride ?: ToothpickProcessorOptions()
        optionsOverride = current.copy(
            crashWhenInjectedMethodIsNotPackageVisible = crashOrWarnWhenMethodIsNotPackageVisible
        )
    }

    @TestOnly
    internal fun getOriginatingElement(generatedQualifiedName: String): TypeElement? {
        return allRoundsGeneratedToTypeElement[generatedQualifiedName]
    }
}