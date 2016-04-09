package com.example.smoothie;

import android.accounts.AccountManager;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.Application;
import android.app.FragmentManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import javax.inject.Inject;
import toothpick.Injector;
import toothpick.ToothPick;
import toothpick.smoothie.module.DefaultActivityModule;

public class SimpleActivity extends Activity {

  @Inject Context context;
  @Inject Application application;
  @Inject AccountManager accountManager;
  @Inject SharedPreferences sharedPreferences;
  @Inject AlarmManager alarmManager;
  @Inject FragmentManager fragmentManager;
  @Inject Activity activity;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.simple_activity);

    Injector classInjector = ToothPick.getOrCreateInjector(getAppInjector(), SimpleActivity.class, new DefaultActivityModule(this));
    classInjector.inject(this);
  }

  private Injector getAppInjector() {
    return ToothPick.getOrCreateInjector(null, Application.class);
  }
}