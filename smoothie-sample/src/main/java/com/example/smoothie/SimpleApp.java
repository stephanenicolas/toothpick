package com.example.smoothie;

import android.app.Application;
import toothpick.Scope;
import toothpick.Toothpick;
import toothpick.registries.FactoryRegistryLocator;
import toothpick.registries.MemberInjectorRegistryLocator;
import toothpick.smoothie.module.SmoothieApplicationModule;

public class SimpleApp extends Application {
  @Override
  public void onCreate() {
    super.onCreate();
    MemberInjectorRegistryLocator.setRootRegistry(new com.example.smoothie.MemberInjectorRegistry());
    FactoryRegistryLocator.setRootRegistry(new com.example.smoothie.FactoryRegistry());
    Scope appScope = Toothpick.openScope(this);
    appScope.installModules(new SmoothieApplicationModule(this));
  }
}