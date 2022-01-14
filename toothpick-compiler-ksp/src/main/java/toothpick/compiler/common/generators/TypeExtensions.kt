package toothpick.compiler.common.generators

import javax.lang.model.element.Element
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.TypeElement
import javax.lang.model.element.VariableElement
import javax.lang.model.type.TypeMirror
import javax.lang.model.util.ElementFilter
import javax.lang.model.util.Types

fun TypeMirror.erased(typeUtils: Types): TypeMirror = typeUtils.erasure(this)
fun TypeMirror.asElement(typeUtils: Types): Element? = typeUtils.asElement(this)

inline fun <reified T : Annotation> Element.hasAnnotation(): Boolean =
    getAnnotation(T::class.java) != null

val Set<Element>.fields: Set<VariableElement>
    get() = ElementFilter.fieldsIn(this)

val Set<Element>.methods: Set<ExecutableElement>
    get() = ElementFilter.methodsIn(this)

val Set<Element>.types: Set<TypeElement>
    get() = ElementFilter.typesIn(this)

val Set<Element>.constructors: Set<ExecutableElement>
    get() = ElementFilter.constructorsIn(this)

val List<Element>.constructors: List<ExecutableElement>
    get() = ElementFilter.constructorsIn(this)
