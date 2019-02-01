package com.example.smoothie;

import android.app.Application;
import toothpick.Scope;
import toothpick.Toothpick;
import toothpick.configuration.Configuration;
import toothpick.smoothie.module.SmoothieApplicationModule;

public class SimpleApp extends Application {
  @Override
  public void onCreate() {
    super.onCreate();

    Toothpick.setConfiguration(Configuration.forProduction());

    Scope appScope = Toothpick.openScope(this);
    initToothpick(appScope);
  }

  public void initToothpick(Scope appScope) {
    appScope.installModules(new SmoothieApplicationModule(this));
  }
}
