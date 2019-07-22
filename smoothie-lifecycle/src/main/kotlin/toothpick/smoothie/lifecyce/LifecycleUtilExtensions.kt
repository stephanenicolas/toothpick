package toothpick.smoothie.lifecycle

import androidx.fragment.app.FragmentActivity
import toothpick.Scope

/**
 * The {@code scope} will be closed automatically during {@code owner}'s {@code onDestroy} event.
 *
 * @param owner the lifecycle owner to observe.
 */
fun Scope.closeOnDestroy(owner: FragmentActivity): Scope {
    LifecycleUtil.closeOnDestroy(owner, this)
    return this
}
