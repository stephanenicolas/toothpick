package com.example.smoothie;

import com.example.smoothie.deps.ContextNamer;
import org.easymock.EasyMockRule;
import org.easymock.Mock;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.annotation.Config;
import toothpick.testing.ToothPickRule;

import static com.google.common.truth.Truth.assertThat;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 21)
public class SimpleActivityTestWithRules {

  //do not use @Rule here, we use a chain below
  public ToothPickRule toothPickRule = new ToothPickRule(this);

  @Rule public TestRule chain = RuleChain.outerRule(toothPickRule).around(new EasyMockRule(this));

  @Mock ContextNamer mockContextNamer;

  @Test
  public void verifyInjectionAtOnCreate() {
    //GIVEN
    expect(mockContextNamer.getApplicationName()).andReturn("foo");
    expect(mockContextNamer.getActivityName()).andReturn("bar");
    replay(mockContextNamer);

    ActivityController<SimpleActivity> controllerSimpleActivity = Robolectric.buildActivity(SimpleActivity.class);
    SimpleActivity activity = controllerSimpleActivity.get();
    toothPickRule.setScopeName(activity);

    //WHEN
    controllerSimpleActivity.create();

    //THEN
    assertThat(activity.title.getText()).isEqualTo("foo");
    assertThat(activity.subTitle.getText()).isEqualTo("bar");
    verify(mockContextNamer);
  }
}