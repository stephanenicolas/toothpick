package com.example.smoothie;

import android.accounts.AccountManager;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.Application;
import android.app.FragmentManager;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.TextView;
import butterknife.Bind;
import butterknife.ButterKnife;
import com.example.smoothie.deps.ContextNamer;
import javax.inject.Inject;
import toothpick.Injector;
import toothpick.ToothPick;
import toothpick.smoothie.module.DefaultActivityModule;

public class LessSimpleActivity extends Activity {

  @Inject Application application;
  @Inject AccountManager accountManager;
  @Inject SharedPreferences sharedPreferences;
  @Inject AlarmManager alarmManager;
  @Inject FragmentManager fragmentManager;
  @Inject Activity activity;
  private Injector injector;

  @Inject ContextNamer contextNamer;
  @Bind(R.id.title) TextView title;
  @Bind(R.id.subtitle) TextView subTitle;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    Injector appInjector = ToothPick.openInjector(getApplication());
    injector = ToothPick.openInjector(this);
    injector.installModules(new DefaultActivityModule(this));
    appInjector.addChildInjector(injector);
    injector.inject(this);
    setContentView(R.layout.simple_activity);
    ButterKnife.bind(this);
    title.setText(contextNamer.getApplicationName());
    subTitle.setText(contextNamer.getActivityName());
  }

  @Override
  protected void onDestroy() {
    ToothPick.closeInjector(this);
    super.onDestroy();
  }
}