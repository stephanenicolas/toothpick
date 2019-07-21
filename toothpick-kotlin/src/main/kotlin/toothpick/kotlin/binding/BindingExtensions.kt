package toothpick.kotlin.binding

import toothpick.config.Binding
import toothpick.config.Module
import kotlin.reflect.KClass
import kotlin.to as toPair

// module
fun module(bindings: Module.() -> Binding<*>?) : Module = Module().apply { bindings() }

// bind
fun Module.bind(key: KClass<Any>) = bind(key.java)
inline fun <reified T> Module.bind() = bind(T::class.java)

// toClass
fun <T> Binding<T>.CanBeBound.toClass(implClass: KClass<out T>) = to(implClass.java)
inline fun <reified T> Binding<T>.CanBeBound.toClass() = to(T::class.java)
