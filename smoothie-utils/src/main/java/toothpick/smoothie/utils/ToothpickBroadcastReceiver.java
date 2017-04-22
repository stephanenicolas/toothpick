package toothpick.smoothie.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import toothpick.Scope;
import toothpick.Toothpick;
import toothpick.config.Module;
import toothpick.smoothie.annotations.ContextSingleton;
import toothpick.smoothie.module.ToothpickBroadcastReceiverModule;
import toothpick.smoothie.module.ToothpickContextModule;

public abstract class ToothpickBroadcastReceiver extends BroadcastReceiver {
  @Override
  public void onReceive(Context context, Intent intent) {
    Scope scope = Toothpick.openScopes(context.getApplicationContext(), this);

    scope.bindScopeAnnotation(ContextSingleton.class);

    scope.installModules(new ToothpickContextModule(context), new ToothpickBroadcastReceiverModule(this));
    Module[] modules = provideModulesForInstallation();
    if(modules != null) {
      scope.installModules(modules);
    }
    Toothpick.inject(this, scope);

    handleReceive(context, intent);

    Toothpick.closeScope(this);
  }

  protected abstract void handleReceive(Context context, Intent intent);

  protected Module[] provideModulesForInstallation() {
    return null;
  }
}
