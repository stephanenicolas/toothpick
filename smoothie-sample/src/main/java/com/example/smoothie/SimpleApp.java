package com.example.smoothie;

import android.app.Application;
import toothpick.Injector;
import toothpick.ToothPick;
import toothpick.smoothie.module.DefaultApplicationModule;

public class SimpleApp extends Application {
  @Override
  public void onCreate() {
    super.onCreate();
    Injector appInjector = ToothPick.openInjector(this);
    appInjector.installModules(new DefaultApplicationModule(this));
  }
}