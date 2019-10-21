package com.example.toothpick.activity

import org.junit.Test

internal class SimpleBackpackItemsActivityTest {
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
    //if injection didn't create, the graph is valid
  }
}
