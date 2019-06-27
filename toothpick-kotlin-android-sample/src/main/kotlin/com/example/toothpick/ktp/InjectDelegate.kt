package com.example.toothpick.ktp

import toothpick.Scope
import java.lang.IllegalStateException
import kotlin.reflect.KProperty

class InjectDelegate<T: Any>(private val clz: Class<T>, private val name: String?) {

    lateinit var instance: T

    fun onEntryPointInjected(scope: Scope) {
        instance = scope.getInstance(clz, name)
    }

    operator fun getValue(thisRef: Any, property: KProperty<*>): T {
        if (this::instance.isInitialized) {
            throw IllegalStateException("The dependency has not be injected yet.")
        }
        return instance
    }
}