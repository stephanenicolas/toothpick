package com.example.smoothie;

import android.app.Application;
import toothpick.ToothPick;
import toothpick.smoothie.module.DefaultApplicationModule;

public class SimpleApp extends Application {
  @Override
  public void onCreate() {
    super.onCreate();
    ToothPick.getOrCreateInjector(null, this, new DefaultApplicationModule(this));
  }
}