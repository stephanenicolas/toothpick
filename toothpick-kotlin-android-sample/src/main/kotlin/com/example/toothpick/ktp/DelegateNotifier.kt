package com.example.toothpick.ktp

import toothpick.Scope
import java.util.*

class DelegateNotifier {

    private val delegatesMap: MutableMap<Any, MutableList<InjectDelegate<out Any>>> = WeakHashMap()

    fun registerDelegate(container: Any, delegate: InjectDelegate<out Any>) {
        val delegates = delegatesMap.getOrPut(container) { mutableListOf() }
        delegates.add(delegate)
    }

    fun notifyDelegates(container: Any, scope: Scope) {
        delegatesMap[container]?.forEach {
            it.onEntryPointInjected(scope)
        }
        delegatesMap.remove(container)
    }
}
