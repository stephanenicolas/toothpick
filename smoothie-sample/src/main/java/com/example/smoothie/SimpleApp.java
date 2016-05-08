package com.example.smoothie;

import android.app.Application;
import toothpick.Scope;
import toothpick.ToothPick;
import toothpick.registries.FactoryRegistryLocator;
import toothpick.registries.MemberInjectorRegistryLocator;
import toothpick.smoothie.module.ApplicationModule;

public class SimpleApp extends Application {
  @Override
  public void onCreate() {
    super.onCreate();
    MemberInjectorRegistryLocator.setRootRegistry(new com.example.smoothie.MemberInjectorRegistry());
    FactoryRegistryLocator.setRootRegistry(new com.example.smoothie.FactoryRegistry());
    Scope appScope = ToothPick.openScope(this);
    appScope.installModules(new ApplicationModule(this));
  }
}