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
import toothpick.Scope;
import toothpick.ToothPick;
import toothpick.smoothie.module.DefaultActivityModule;

public class LessSimpleActivity extends Activity {

  @Inject Application application;
  @Inject AccountManager accountManager;
  @Inject SharedPreferences sharedPreferences;
  @Inject AlarmManager alarmManager;
  @Inject FragmentManager fragmentManager;
  @Inject Activity activity;
  private Scope scope;

  @Inject ContextNamer contextNamer;
  @Bind(R.id.title) TextView title;
  @Bind(R.id.subtitle) TextView subTitle;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    Scope appScope = ToothPick.openScope(getApplication());
    scope = ToothPick.openScope(this);
    scope.installModules(new DefaultActivityModule(this));
    appScope.addChild(scope);
    ToothPick.inject(this, scope);
    setContentView(R.layout.simple_activity);
    ButterKnife.bind(this);
    title.setText(contextNamer.getApplicationName());
    subTitle.setText(contextNamer.getActivityName());
  }

  @Override
  protected void onDestroy() {
    ToothPick.closeScope(this);
    super.onDestroy();
  }
}