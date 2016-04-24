package toothpick.smoothie.module;

import android.app.Activity;
import android.app.Application;
import android.app.FragmentManager;
import android.app.LoaderManager;
import android.view.LayoutInflater;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import toothpick.Scope;
import toothpick.ToothPick;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

@RunWith(RobolectricTestRunner.class)
public class ActivityModuleTest {

  @Test
  public void testGet() throws Exception {
    //GIVEN
    Activity activity = Robolectric.buildActivity(Activity.class).create().get();
    Application application = Robolectric.application;
    Scope appScope = ToothPick.openScope(application);
    appScope.installModules(new ApplicationModule(application));

    Scope activityScope = ToothPick.openScopes(application, activity);
    activityScope.installModules(new ActivityModule(activity));

    //WHEN
    Activity injectedActivity = activityScope.getInstance(Activity.class);
    FragmentManager fragmentManager = activityScope.getInstance(FragmentManager.class);
    LoaderManager loaderManager = activityScope.getInstance(LoaderManager.class);
    LayoutInflater layoutInflater = activityScope.getInstance(LayoutInflater.class);

    //THEN
    assertThat(injectedActivity, is(activity));
    assertThat(fragmentManager, notNullValue());
    assertThat(loaderManager, notNullValue());
    assertThat(layoutInflater, notNullValue());
  }
}