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
import toothpick.Scope;
import toothpick.ToothPick;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.MatcherAssert.assertThat;

@RunWith(RobolectricTestRunner.class)
public class SupportActivityModuleTest {

  @Test
  public void testGet() throws Exception {
    //GIVEN
    FragmentActivity activity = Robolectric.buildActivity(FragmentActivity.class).create().get();
    Application application = Robolectric.application;
    Scope appScope = ToothPick.openScope(application);
    appScope.installModules(new ApplicationModule(application));

    Scope activityScope = ToothPick.openScopes(application, activity);
    activityScope.installModules(new SupportActivityModule(activity));

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