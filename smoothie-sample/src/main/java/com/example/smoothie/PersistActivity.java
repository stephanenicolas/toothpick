package com.example.smoothie;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import butterknife.Bind;
import butterknife.ButterKnife;
import com.example.smoothie.deps.ContextNamer;
import javax.inject.Inject;
import toothpick.Injector;
import toothpick.ToothPick;
import toothpick.config.Module;
import toothpick.smoothie.module.DefaultActivityModule;

public class PersistActivity extends Activity {

  public static final String PRESENTER_SCOPE = "PRESENTER_SCOPE";
  private Injector injector;

  @Inject ContextNamer contextNamer;
  @Bind(R.id.title) TextView title;
  @Bind(R.id.subtitle) TextView subTitle;
  @Bind(R.id.hello) Button button;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    Injector appInjector = ToothPick.openInjector(getApplication());
    Injector metaInjector = ToothPick.openInjector(PRESENTER_SCOPE);
    appInjector.addChildInjector(metaInjector);
    injector = ToothPick.openInjector(this);
    metaInjector.addChildInjector(injector);
    injector.installModules(new DefaultActivityModule(this));
    injector.inject(this);
    metaInjector.installModules(new PresenterModule());
    setContentView(R.layout.simple_activity);
    ButterKnife.bind(this);
    title.setText("Persist");
    subTitle.setText(contextNamer.getInstanceCount());
    button.setVisibility(View.GONE);
  }

  @Override
  protected void onDestroy() {
    ToothPick.closeInjector(this);
    super.onDestroy();
  }

  @Override
  public void onBackPressed() {
    ToothPick.closeInjector(PRESENTER_SCOPE);
    super.onBackPressed();
  }

  private class PresenterModule extends Module {
    {
      bind(ContextNamer.class).to(contextNamer);
    }
  }
}