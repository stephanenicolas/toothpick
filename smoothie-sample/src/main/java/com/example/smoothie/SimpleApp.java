package com.example.smoothie;

import android.app.Application;
import toothpick.Scope;
import toothpick.ToothPick;
import toothpick.smoothie.module.ApplicationModule;

public class SimpleApp extends Application {
  @Override
  public void onCreate() {
    super.onCreate();
    Scope appScope = ToothPick.openScope(this);
    appScope.installModules(new ApplicationModule(this));
  }
}