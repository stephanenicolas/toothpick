package com.example.toothpick.ktp

import kotlin.reflect.KProperty

class DelegateProvider<T: Any>(val clz: Class<T>, val name: String?) {

    operator fun provideDelegate(thisRef: Any, prop: KProperty<*>): InjectDelegate<T> {
        val delegate = InjectDelegate(clz, name)
        KTP.delegateNotifier.registerDelegate(thisRef, delegate)
        return delegate
    }
}