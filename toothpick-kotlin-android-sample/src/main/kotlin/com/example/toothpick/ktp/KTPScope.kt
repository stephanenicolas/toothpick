package com.example.toothpick.ktp

import toothpick.Scope

class KTPScope(val scope: Scope): Scope by scope {

    override fun inject(obj: Any?) {
        obj?.let {
            KTP.delegateNotifier.notifyDelegates(it, scope)
        }
    }
}