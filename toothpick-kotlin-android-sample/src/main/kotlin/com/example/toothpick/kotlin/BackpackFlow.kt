package com.example.toothpick.kotlin

import toothpick.Toothpick
import toothpick.ktp.KTP
import kotlin.annotation.AnnotationRetention.RUNTIME

class BackpackFlow {

    companion object {
        fun openScope(parentScope: toothpick.Scope): toothpick.Scope {
            return parentScope.openSubScope(Scope::class.java)
        }

        fun closeScope() {
            KTP.closeScope(Scope::class.java)
        }
    }

    @javax.inject.Scope
    @Retention(RUNTIME)
    @Target(AnnotationTarget.CLASS)
    annotation class Scope
}

fun toothpick.Scope.openBackpackFlowSubScope(): toothpick.Scope {
    return BackpackFlow.openScope(this)
}


