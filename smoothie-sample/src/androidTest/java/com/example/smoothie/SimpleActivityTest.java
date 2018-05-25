package com.example.smoothie;

import android.support.test.InstrumentationRegistry;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import com.example.smoothie.deps.ContextNamer;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import toothpick.Scope;
import toothpick.Toothpick;
import toothpick.config.Module;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

@RunWith(AndroidJUnit4.class)
public class SimpleActivityTest {

    @Rule public ActivityTestRule<SimpleActivity> activityRule = new ActivityTestRule<>(SimpleActivity.class, false, false);

    private Scope appScope;
    private SimpleApp application;

    @Before
    public void setup() {
        application = (SimpleApp) InstrumentationRegistry.getTargetContext().getApplicationContext();
        appScope = Toothpick.openScope(application);
    }

    @After
    public void tearDown() {
        Toothpick.reset(appScope);
        application.initToothpick(appScope);
    }

    @Test
    public void testNormal() {
        activityRule.launchActivity(null);

        onView(withId(R.id.title))
            .check(matches(withText("SimpleApp")));

        onView(withId(R.id.subtitle))
            .check(matches(withText("SimpleActivity")));
    }

    @Test
    public void testWithContextNamerTestModule() {
        appScope.installTestModules(new ContextNamerTestModule(new TestContextNamer("TestAppName", "TestActivityName")));

        activityRule.launchActivity(null);

        onView(withId(R.id.title))
            .check(matches(withText("TestAppName")));

        onView(withId(R.id.subtitle))
            .check(matches(withText("TestActivityName")));
    }

    @Test
    public void testWithContextNamerTestModuleWithMock() {
        ContextNamer contextNamer = createMock(ContextNamer.class);

        expect(contextNamer.getApplicationName()).andReturn("TestAppNameMock");
        expect(contextNamer.getActivityName()).andReturn("TestActivityNameMock");

        replay(contextNamer);

        appScope.installTestModules(new ContextNamerTestModule(contextNamer));

        activityRule.launchActivity(null);

        verify(contextNamer);

        onView(withId(R.id.title))
            .check(matches(withText("TestAppNameMock")));

        onView(withId(R.id.subtitle))
            .check(matches(withText("TestActivityNameMock")));
    }

    private static class ContextNamerTestModule extends Module {
        ContextNamerTestModule(ContextNamer contextNamer) {
            bind(ContextNamer.class).toInstance(contextNamer);
        }
    }

    private static class TestContextNamer extends ContextNamer {

        private final String appName;
        private final String activityName;

        TestContextNamer(String appName, String activityName) {
            this.appName = appName;
            this.activityName = activityName;
        }

        @Override
        public String getApplicationName() {
            return appName;
        }

        @Override
        public String getActivityName() {
            return activityName;
        }
    }
}
