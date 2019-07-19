package toothpick.kotlin.binding

import toothpick.config.Binding
import toothpick.config.Module
import kotlin.reflect.KClass

// module
fun module(bindings: Module.() -> Binding<*>?) : Module = Module().apply { bindings() }

// bind
fun Module.bind(key: KClass<Any>) = bind(key.java)
inline fun <reified T> Module.bind() = bind(T::class.java)

// toClass
