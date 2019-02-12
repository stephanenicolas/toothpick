package toothpick.kotlin.androidx

import android.app.Activity
import android.view.View
import androidx.fragment.app.Fragment
import toothpick.Scope
import toothpick.Toothpick
import toothpick.kotlin.InputConfigBlock
import toothpick.kotlin.ToothpickInjectDelegate
import kotlin.reflect.KClass
import kotlin.reflect.KProperty

fun View.openScope(): Scope {
    return ContextScopeFinder.findScope(context)
}

fun View.inject() {
    Toothpick.inject(this, openScope())
}

inline fun <reified T : Any> Activity.inject(
    scope: Scope? = null,
    name: String? = null,
    annotationName: KClass<out Annotation>? = null,
    noinline injectConfig: InputConfigBlock? = null
): ActivityInjectDelegate<T> {
    return ActivityInjectDelegate(T::class, scope, name, annotationName, injectConfig)
}

inline fun <reified T : Any> Fragment.inject(
    scope: Scope? = null,
    name: String? = null,
    annotationName: KClass<out Annotation>? = null,
    noinline injectConfig: InputConfigBlock? = null
): FragmentInjectDelegate<T> {
    return FragmentInjectDelegate(T::class, scope, name, annotationName, injectConfig)
}

class ActivityInjectDelegate<T : Any>(
    clazz: KClass<T>,
    scope: Scope? = null,
    name: String? = null,
    annotationName: KClass<out Annotation>? = null,
    injectConfig: InputConfigBlock? = null
) : ToothpickInjectDelegate<Activity, T>(clazz, scope, name, annotationName, injectConfig) {
    override fun getScope(thisRef: Activity, property: KProperty<*>): Scope {
        return super.getScope(thisRef, property) ?: Toothpick.openScopes(thisRef.application, thisRef)
    }
}

class FragmentInjectDelegate<T : Any>(
    clazz: KClass<T>,
    scope: Scope? = null,
    name: String? = null,
    annotationName: KClass<out Annotation>? = null,
    injectConfig: InputConfigBlock? = null
) : ToothpickInjectDelegate<Fragment, T>(clazz, scope, name, annotationName, injectConfig) {
    override fun getScope(thisRef: Fragment, property: KProperty<*>): Scope {
        return super.getScope(thisRef, property) ?: Toothpick.openScopes(thisRef.activity?.application, thisRef.activity!!)
    }
}
