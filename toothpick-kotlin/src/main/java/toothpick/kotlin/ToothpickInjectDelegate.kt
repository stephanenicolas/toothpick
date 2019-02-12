package toothpick.kotlin

import toothpick.Scope
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KClass
import kotlin.reflect.KProperty

typealias InputConfigBlock = (InjectConfig.() -> Unit)

open class ToothpickInjectDelegate<OWNER, T : Any>(
    protected val clazz: KClass<T>,
    protected val scope: Scope?,
    protected val name: String? = null,
    protected val annotationName: KClass<out Annotation>? = null,
    protected val injectConfigBlock: InputConfigBlock? = null
) : ReadOnlyProperty<OWNER, T> {
    private var value: T? = null

    override fun getValue(thisRef: OWNER, property: KProperty<*>): T {
        return value ?: injectValue(thisRef, property)
    }

    protected open fun injectValue(thisRef: OWNER, property: KProperty<*>): T {
        val injectConfig = InjectConfig()
        if (injectConfigBlock != null) {
            injectConfig.apply(injectConfigBlock)
        }
        val scope = injectConfig.scope ?: scope ?: getScope(thisRef, property) ?: throw IllegalStateException("No valid scope available when attempting to inject $property in $thisRef")
        return scope.getInstance(clazz, injectConfig.name ?: name, injectConfig.annotationName ?: annotationName).also { value = it }
    }

    protected open fun getScope(thisRef: OWNER, property: KProperty<*>): Scope? {
        if (scope != null) {
            return scope
        }

        if (thisRef is HasScope) {
            return thisRef.scope
        }

        return null
    }
}

class InjectConfig(var scope: Scope? = null, var name: String? = null, var annotationName: KClass<out Annotation>? = null)
