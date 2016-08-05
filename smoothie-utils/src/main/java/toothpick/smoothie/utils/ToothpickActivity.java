package toothpick.smoothie.utils;

import android.app.Activity;
import android.os.Bundle;
import toothpick.Scope;
import toothpick.Toothpick;
import toothpick.config.Module;
import toothpick.smoothie.annotations.ActivitySingleton;
import toothpick.smoothie.annotations.ContextSingleton;
import toothpick.smoothie.module.ToothpickActivityModule;
import toothpick.smoothie.module.ToothpickContextModule;

public abstract class ToothpickActivity extends Activity {
  private Scope scope;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    scope = Toothpick.openScopes(getApplication(), this);

    scope.bindScopeAnnotation(ContextSingleton.class);
    scope.bindScopeAnnotation(ActivitySingleton.class);

    scope.installModules(new ToothpickContextModule(this), new ToothpickActivityModule(this));
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