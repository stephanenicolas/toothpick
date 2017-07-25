package toothpick.smoothie.module;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;
import toothpick.Scope;
import toothpick.Toothpick;

import static toothpick.Toothpick.closeScope;
import static toothpick.Toothpick.openScope;

public class Smoothie {

  private Smoothie() {
  }

  public static void autoCloseActivityScope(Activity activity) {
    Application application = activity.getApplication();
    application.registerActivityLifecycleCallbacks(
        new AutoCloseActivityLifecycleCallbacks(activity));
  }

  public static void autoCloseActivityScopes(Application application) {
    application.registerActivityLifecycleCallbacks(new AutoCloseActivitiesLifecycleCallbacks());
  }

  public static void autoCloseActivityScopeAndParentScope(Activity activity) {
    Application application = activity.getApplication();
    application.registerActivityLifecycleCallbacks(
        new AutoCloseActivityParentLifecycleCallbacks(activity));
  }

  private static class AutoCloseActivityLifecycleCallbacks extends DefaultLifecycleCallbacks {
    private Activity targetActivity;

    public AutoCloseActivityLifecycleCallbacks(Activity activity) {
      targetActivity = activity;
    }

    @Override public void onActivityDestroyed(Activity activity) {
      if (activity == targetActivity) {
        closeScope(activity);
      }
      final Application application = activity.getApplication();
      application.unregisterActivityLifecycleCallbacks(this);
    }
  }

  private static class AutoCloseActivityParentLifecycleCallbacks extends DefaultLifecycleCallbacks {
    private Activity targetActivity;

    public AutoCloseActivityParentLifecycleCallbacks(Activity activity) {
      targetActivity = activity;
    }

    @Override public void onActivityDestroyed(Activity activity) {
      if (activity == targetActivity) {
        closeScope(activity);
        if(activity.isFinishing()) {
          final Scope parentScope = openScope(activity).getParentScope();
          closeScope(parentScope.getName());
        }
      }
      final Application application = activity.getApplication();
      application.unregisterActivityLifecycleCallbacks(this);
    }
  }

  private static class AutoCloseActivitiesLifecycleCallbacks extends DefaultLifecycleCallbacks {
    @Override public void onActivityDestroyed(Activity activity) {
      closeScope(activity);
    }
  }

  private static class DefaultLifecycleCallbacks implements Application.ActivityLifecycleCallbacks {
    @Override public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
    }

    @Override public void onActivityStarted(Activity activity) {
    }

    @Override public void onActivityResumed(Activity activity) {
    }

    @Override public void onActivityPaused(Activity activity) {
    }

    @Override public void onActivityStopped(Activity activity) {
    }

    @Override public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
    }

    @Override public void onActivityDestroyed(Activity activity) {
    }
  }
}
