/*
 * Copyright 2019 Stephane Nicolas
 * Copyright 2019 Daniel Molinero Reguera
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package toothpick.smoothie.module;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import android.app.Activity;
import android.app.Application;
import android.view.LayoutInflater;
import androidx.test.core.app.ApplicationProvider;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import toothpick.Scope;
import toothpick.Toothpick;

@RunWith(RobolectricTestRunner.class)
@SuppressWarnings("deprecation")
public class SmoothieActivityModuleTest {

  @Test
  public void testGet() {
    // GIVEN
    Activity activity = Robolectric.buildActivity(Activity.class).create().get();
    Application application = ApplicationProvider.getApplicationContext();
    Scope appScope = Toothpick.openScope(application);
    appScope.installModules(new SmoothieApplicationModule(application));

    Scope activityScope = Toothpick.openScopes(application, activity);
    activityScope.installModules(new SmoothieActivityModule(activity));

    // WHEN
    Activity injectedActivity = activityScope.getInstance(Activity.class);
    android.app.FragmentManager fragmentManager =
        activityScope.getInstance(android.app.FragmentManager.class);
    android.app.LoaderManager loaderManager =
        activityScope.getInstance(android.app.LoaderManager.class);
    LayoutInflater layoutInflater = activityScope.getInstance(LayoutInflater.class);

    // THEN
    assertThat(injectedActivity, is(activity));
    assertThat(fragmentManager, notNullValue());
    assertThat(loaderManager, notNullValue());
    assertThat(layoutInflater, notNullValue());
  }
}
