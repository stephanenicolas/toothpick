package com.example.smoothie;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import butterknife.BindView;
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
  @BindView(R.id.title) TextView title;
  @BindView(R.id.subtitle) TextView subTitle;
  @BindView(R.id.hello) Button button;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    scope = ToothPick.openScopes(getApplication(), this);
    scope.installModules(new ActivityModule(this));
    super.onCreate(savedInstanceState);
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
    startActivity(new Intent(this, RxMVPActivity.class));
  }

  @Override
  protected void onDestroy() {
    ToothPick.closeScope(this);
    super.onDestroy();
  }
}