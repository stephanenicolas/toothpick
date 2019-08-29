package toothpick

import toothpick.ktp.KTP

/**
 * Replaces Toothpick injector to be able to notify delegates.
 */
object InjectorReplacer {

    fun replace() {
        Toothpick.injector = object : InjectorImpl() {
            override fun <T : Any> inject(obj: T, scope: Scope) {
                if (KTP.delegateNotifier.hasDelegates(obj)) {
                    KTP.delegateNotifier.notifyDelegates(obj, scope)
                }
                super.inject(obj, scope)
            }
        }
    }
}
