package toothpick.smoothie.lifecycle;

import androidx.annotation.NonNull;
import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.LifecycleOwner;
import toothpick.Scope;

import static toothpick.Toothpick.closeScope;

public class Lifecycle {

    private Lifecycle() {
        throw new RuntimeException("Should not be instantiated");
    }

    /**
     * The {@code scope} will be closed automatically during {@code owner}'s {@code onDestroy} event.
     * @param owner the lifecycle owner to observe.
     * @param scope the scope to be closed automatically during {@code owner}'s {@code onDestroy} event.
     */
    public static void closeOnDestroy(final @NonNull LifecycleOwner owner, final @NonNull Scope scope) {
        owner.getLifecycle().addObserver(new DefaultLifecycleObserver() {
            @Override public void onDestroy(@NonNull LifecycleOwner owner) {
                closeScope(scope.getName());
            }
        });
    }
}
