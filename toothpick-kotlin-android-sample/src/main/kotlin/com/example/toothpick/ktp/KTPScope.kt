package com.example.toothpick.ktp

import toothpick.Scope
import toothpick.config.Module

class KTPScope(private val scope: Scope): Scope by scope {

    override fun inject(obj: Any?) {
        obj?.let {
            KTP.delegateNotifier.notifyDelegates(it, scope)
        }
    }

    override fun installModules(vararg modules: Module?): Scope {
        scope.installModules(*modules)
        return this
    }
}