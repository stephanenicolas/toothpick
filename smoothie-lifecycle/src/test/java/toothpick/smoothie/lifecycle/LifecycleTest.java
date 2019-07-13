package toothpick.smoothie.lifecycle;

import android.app.Application;
import androidx.fragment.app.FragmentActivity;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.android.controller.ActivityController;
import toothpick.Scope;
import toothpick.Toothpick;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

@RunWith(RobolectricTestRunner.class)
public class LifecycleTest {
    @Test
    public void testCloseOnDestroy() {
        // GIVEN
        ActivityController<FragmentActivity> activityController = Robolectric.buildActivity(FragmentActivity.class).create();
        FragmentActivity activity = activityController.get();
        Application application = RuntimeEnvironment.application;
        Scope activityScope = Toothpick.openScopes(application, activity);

        // WHEN
        Lifecycle.closeOnDestroy(activity, activityScope);
        activityController.destroy();

        // THEN
        assertThat(Toothpick.isScopeOpen(activity), is(false));
    }
}
