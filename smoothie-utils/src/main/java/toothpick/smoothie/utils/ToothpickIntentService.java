package toothpick.smoothie.utils;

import android.app.IntentService;
import toothpick.Scope;
import toothpick.Toothpick;
import toothpick.config.Module;
import toothpick.smoothie.annotations.ContextSingleton;
import toothpick.smoothie.annotations.ServiceSingleton;
import toothpick.smoothie.module.ToothpickContextModule;
import toothpick.smoothie.module.ToothpickServiceModule;

public abstract class ToothpickIntentService extends IntentService {
  private Scope scope;

  public ToothpickIntentService() {
    super("ToothpickIntentService");
  }

  public ToothpickIntentService(String name) {
    super(name);
  }

  @Override
  public void onCreate() {
    scope = Toothpick.openScopes(getApplication(), this);

    scope.bindScopeAnnotation(ContextSingleton.class);
    scope.bindScopeAnnotation(ServiceSingleton.class);

    scope.installModules(new ToothpickContextModule(this), new ToothpickServiceModule(this));
    Module[] modules = provideModulesForInstallation();
    if(modules != null) {
      scope.installModules(modules);
    }
    super.onCreate();
    Toothpick.inject(this, scope);
  }

  protected Module[] provideModulesForInstallation() {
    return null;
  }

  @Override
  public void onDestroy() {
    Toothpick.closeScope(this);
    super.onDestroy();
  }

  protected Scope getScope() {
    return scope;
  }
}
