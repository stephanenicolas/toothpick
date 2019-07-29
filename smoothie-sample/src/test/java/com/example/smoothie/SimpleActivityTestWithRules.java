package com.example.smoothie;

import com.example.smoothie.deps.ContextNamer;
import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.android.controller.ActivityController;
import toothpick.Toothpick;
import toothpick.testing.ToothPickRule;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

@RunWith(RobolectricTestRunner.class)
public class SimpleActivityTestWithRules {

  @Rule(order = 1)
  public ToothPickRule toothPickRule = new ToothPickRule(this);

  @Rule(order = 2)
  public MockitoRule mockitoJUnitRule = MockitoJUnit.rule();

  @Mock ContextNamer mockContextNamer;

  @Test
  public void verifyInjectionAtOnCreate() {
    //GIVEN
    when(mockContextNamer.getApplicationName()).thenReturn("foo");
    when(mockContextNamer.getActivityName()).thenReturn("bar");

    ActivityController<SimpleActivity> controllerSimpleActivity = Robolectric.buildActivity(SimpleActivity.class);
    SimpleActivity activity = controllerSimpleActivity.get();
    toothPickRule.setScopeName(activity);

    //WHEN
    controllerSimpleActivity.create();

    //THEN
    assertThat(activity.title.getText()).isEqualTo("foo");
    assertThat(activity.subTitle.getText()).isEqualTo("bar");
    verify(mockContextNamer).getApplicationName();
    verify(mockContextNamer).getActivityName();
  }
}