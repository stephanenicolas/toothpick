package com.example.smoothie;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import com.example.smoothie.deps.ContextNamer;
import javax.inject.Inject;
import toothpick.Scope;
import toothpick.ToothPick;
import toothpick.smoothie.module.ActivityModule;

public class SimpleActivity extends Activity {

  private Scope scope;

  @Inject ContextNamer contextNamer;
  @Bind(R.id.title) TextView title;
  @Bind(R.id.subtitle) TextView subTitle;
  @Bind(R.id.hello) Button button;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    //Smoothie.openApplicationScope(getApplication(), <modules...>) no need to include application module
    //.openActivityScope(this, <modules...>) no need to include activity module
    //.inject(this) // all DSL scope state can inject
    scope = ToothPick.openScopes(getApplication(), this);
    scope.installModules(new ActivityModule(this));
    ToothPick.inject(this, scope);
    setContentView(R.layout.simple_activity);
    ButterKnife.bind(this);
    title.setText(contextNamer.getApplicationName());
    subTitle.setText(contextNamer.getActivityName());
    button.setText("click me !");
  }

  @OnClick(R.id.hello)
  @SuppressWarnings("unused")
  void startNewActivity() {
    startActivity(new Intent(this, PersistActivity.class));
  }

  @Override
  protected void onDestroy() {
    ToothPick.closeScope(this);
    super.onDestroy();
  }
}