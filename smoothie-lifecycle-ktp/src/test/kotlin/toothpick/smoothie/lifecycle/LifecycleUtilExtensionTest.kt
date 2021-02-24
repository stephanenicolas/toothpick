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
package toothpick.smoothie.lifecycle

import android.content.Context
import androidx.fragment.app.FragmentActivity
import androidx.test.core.app.ApplicationProvider
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import toothpick.Toothpick
import toothpick.ktp.KTP

@RunWith(RobolectricTestRunner::class)
class LifecycleUtilExtensionTest {
    @Test
    fun testCloseOnDestroy() {
        // GIVEN
        val activityController = Robolectric.buildActivity(FragmentActivity::class.java).create()
        val activity = activityController.get()
        val application: Context = ApplicationProvider.getApplicationContext()

        // WHEN
        KTP.openScopes(application, activity)
                .closeOnDestroy(activity)
        activityController.destroy()

        // THEN
        assertThat(Toothpick.isScopeOpen(activity), `is`(false))
    }
}
