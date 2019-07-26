package com.example.smoothie;

import com.example.smoothie.deps.ContextNamer;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.android.controller.ActivityController;
import toothpick.Scope;
import toothpick.Toothpick;
import toothpick.config.Module;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

@RunWith(RobolectricTestRunner.class)
public class SimpleActivityTest {

  @After
  public void tearDown() throws Exception {
    Toothpick.reset();
  }

  @Test
  public void verifyInjectionAtOnCreate() {
    //GIVEN
    final ContextNamer mockContextNamer = mock(ContextNamer.class);
    when(mockContextNamer.getApplicationName()).thenReturn("foo");
    when(mockContextNamer.getActivityName()).thenReturn("bar");

    ActivityController<SimpleActivity> controllerSimpleActivity = Robolectric.buildActivity(SimpleActivity.class);
    SimpleActivity activity = controllerSimpleActivity.get();
    Scope scope = Toothpick.openScope(activity);
    //or new ToothPickTestModule(this)
    scope.installTestModules(new TestModule(mockContextNamer));

    //WHEN
    controllerSimpleActivity.create();

    //THEN
    assertThat(activity.title.getText()).isEqualTo("foo");
    assertThat(activity.subTitle.getText()).isEqualTo("bar");
    verify(mockContextNamer).getApplicationName();
    verify(mockContextNamer).getActivityName();
  }

  private class TestModule extends Module {
    public TestModule(ContextNamer mockContextNamer) {
      bind(ContextNamer.class).toInstance(mockContextNamer);
    }
  }
}