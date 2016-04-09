package com.example.smoothie;

import android.app.Application;
import toothpick.Injector;
import toothpick.ToothPick;
import toothpick.smoothie.module.DefaultApplicationModule;

public class SimpleApp extends Application {
  @Override
  public void onCreate() {
    super.onCreate();

    // Create App Injector
    Injector appInjector = ToothPick.getOrCreateInjector(null, Application.class, new DefaultApplicationModule(this));
  }
}