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
package toothpick.smoothie.viewmodel

import android.content.Context
import android.content.res.Configuration
import androidx.fragment.app.FragmentActivity
import androidx.test.core.app.ApplicationProvider
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.sameInstance
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import toothpick.Toothpick.isScopeOpen
import toothpick.ktp.KTP
import toothpick.ktp.binding.bind
import toothpick.ktp.binding.module

@RunWith(RobolectricTestRunner::class)
class ViewModelUtilExtensionTest {
    @Test
    fun testCloseOnClear() {
        // GIVEN
        val activityController = Robolectric.buildActivity(FragmentActivity::class.java).create()
        val activity = activityController.get()
        val application: Context = ApplicationProvider.getApplicationContext()

        // WHEN
        KTP.openScopes(application, activity)
            .closeOnViewModelCleared(activity)
        activityController.destroy()

        // THEN
        assertThat(isScopeOpen(activity), `is`(false))
    }

    @Test
    fun testCloseOnClear_shouldNotCloseScope_whenConfigurationChange() {
        // GIVEN
        val activityController = Robolectric.buildActivity(FragmentActivity::class.java).create()
        val activity = activityController.get()
        val application: Context = ApplicationProvider.getApplicationContext()

        // WHEN
        KTP.openScopes(application, activity)
            .closeOnViewModelCleared(activity)
        activityController.configurationChange(Configuration())

        // THEN
        assertThat(isScopeOpen(activity), `is`(true))
    }

    @Test
    fun testCloseOnClear_shouldPreserveViewModel_whenConfigurationChange() {
        // GIVEN
        val activityController = Robolectric.buildActivity(FragmentActivity::class.java).create()
        val activity = activityController.get()
        val application: Context = ApplicationProvider.getApplicationContext()
        // WHEN
        val scope = KTP.openScopes(application, ViewModelScope::class.java)
            .installViewModelBinding<TestViewModel>(activity)
            .closeOnViewModelCleared(activity)
            .installModules(module {
                bind<String>().withName("name").toInstance { "dependency" }
            })
            .openSubScope(activity)

        val viewModelBeforeRotation = scope.getInstance(TestViewModel::class.java)
        activityController.configurationChange(Configuration())
        val viewModelAfterRotation = scope.getInstance(TestViewModel::class.java)

        // THEN
        assertThat(isScopeOpen(activity), `is`(true))
        assertThat(viewModelAfterRotation, sameInstance(viewModelBeforeRotation))
        assertThat(viewModelBeforeRotation.string, `is`("dependency"))
    }
}
