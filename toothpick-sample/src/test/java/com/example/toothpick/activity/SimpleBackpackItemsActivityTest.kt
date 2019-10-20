package com.example.toothpick.activity

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.jupiter.api.extension.RegisterExtension
import toothpick.testing.ToothPickExtension

internal class SimpleBackpackItemsActivityTest {

  @field:RegisterExtension
  var toothPickRule = ToothPickExtension(this)

  /**
   * In this test, we demonstrate how to unit test
   * a call to inject on an entry point such as an activity.
   * This test validates the whole dependency sub-graph, it is the equivalent
   * for TP to the compilation checks performed by Dagger.
   */
  @Test
  fun testInjections() {
    //GIVEN
    val activity = SimpleBackpackItemsActivity()

    //WHEN
    activity.injectDependencies()

    //TEST
    assertThat(activity.backpack).isNotNull()
    assertThat(activity.notificationHelper).isNotNull()
    assertThat(activity.viewAdapter).isNotNull()
  }

}