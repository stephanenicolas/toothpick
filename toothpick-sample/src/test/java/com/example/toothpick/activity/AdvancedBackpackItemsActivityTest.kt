package com.example.toothpick.activity

import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.spy
import org.junit.Test

internal class AdvancedBackpackItemsActivityTest {

  /**
   * In this test, we demonstrate how to unit test
   * a call to inject on an entry point such as an activity.
   * This test validates the whole dependency sub-graph, it is the equivalent
   * for TP to the compilation checks performed by Dagger.
   * As the activity under test is using view models, we need some
   * extra steps to mock the final {@link Activity#getApplication()} method.
   * We also add to add testOptions.unitTests.returnDefaultValues = true in the build file.
   * And support final method mocking from mockito with the file org.mockito.plugins.MockMaker.
   */
  @Test
  fun testInjections() {
    //GIVEN
    val mockActivity = spy<AdvancedBackpackItemsActivity> {
      on { application } doReturn mock()
    }

    //WHEN
    mockActivity.injectDependencies()

    //TEST
    //if injection didn't create, the graph is valid
  }
}
