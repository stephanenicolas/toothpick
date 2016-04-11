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
import toothpick.Injector;
import toothpick.ToothPick;
import toothpick.smoothie.module.DefaultActivityModule;

public class SimpleActivity extends Activity {

  private Injector injector;

  @Inject ContextNamer contextNamer;
  @Bind(R.id.title) TextView title;
  @Bind(R.id.subtitle) TextView subTitle;
  @Bind(R.id.hello) Button button;

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
    button.setText("click me !");
  }

  @OnClick(R.id.hello)
  @SuppressWarnings("unused")
  void startNewActivity() {
    startActivity(new Intent(this, PersistActivity.class));
  }

  @Override
  protected void onDestroy() {
    ToothPick.closeInjector(this);
    super.onDestroy();
  }
}