package toothpick.smoothie.utils;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import toothpick.Scope;
import toothpick.Toothpick;
import toothpick.config.Module;
import toothpick.smoothie.annotations.ActivitySingleton;
import toothpick.smoothie.annotations.ContextSingleton;
import toothpick.smoothie.module.ToothpickSupportActivityModule;
import toothpick.smoothie.module.ToothpickContextModule;

public abstract class ToothpickFragmentActivity extends FragmentActivity {
  private Scope scope;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    scope = Toothpick.openScopes(getApplication(), this);

    scope.bindScopeAnnotation(ContextSingleton.class);
    scope.bindScopeAnnotation(ActivitySingleton.class);

    scope.installModules(new ToothpickContextModule(this), new ToothpickSupportActivityModule(this));
    Module[] modules = provideModulesForInstallation();
    if(modules != null) {
      scope.installModules(modules);
    }
    super.onCreate(savedInstanceState);
    Toothpick.inject(this, scope);
  }

  protected Module[] provideModulesForInstallation() {
    return null;
  }

  @Override
  protected void onDestroy() {
    Toothpick.closeScope(this);
    super.onDestroy();
  }

  protected Scope getScope() {
    return scope;
  }
}