package toothpick.smoothie.module;

import android.app.Activity;
import android.app.Application;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.LoaderManager;
import android.view.LayoutInflater;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import toothpick.Scope;
import toothpick.Toothpick;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.MatcherAssert.assertThat;

@RunWith(RobolectricTestRunner.class)
public class SmoothieSupportActivityModuleTest {

  @Test
  public void testGet() throws Exception {
    //GIVEN
    FragmentActivity activity = Robolectric.buildActivity(FragmentActivity.class).create().get();
    Application application = RuntimeEnvironment.application;
    Scope appScope = Toothpick.openScope(application);
    appScope.installModules(new SmoothieApplicationModule(application));

    Scope activityScope = Toothpick.openScopes(application, activity);
    activityScope.installModules(new SmoothieSupportActivityModule(activity));

    //WHEN
    Activity injectedActivity = activityScope.getInstance(Activity.class);
    FragmentManager fragmentManager = activityScope.getInstance(FragmentManager.class);
    LoaderManager loaderManager = activityScope.getInstance(LoaderManager.class);
    LayoutInflater layoutInflater = activityScope.getInstance(LayoutInflater.class);

    //THEN
    assertThat(injectedActivity, instanceOf(FragmentActivity.class));
    assertThat((FragmentActivity) injectedActivity, sameInstance(activity));
    assertThat(fragmentManager, notNullValue());
    assertThat(loaderManager, notNullValue());
    assertThat(layoutInflater, notNullValue());
  }
}