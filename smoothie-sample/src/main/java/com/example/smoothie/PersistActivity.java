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
import toothpick.Scope;
import toothpick.ToothPick;
import toothpick.config.Module;
import toothpick.smoothie.module.DefaultActivityModule;

public class PersistActivity extends Activity {

  public static final String PRESENTER_SCOPE = "PRESENTER_SCOPE";
  private Scope scope;

  @Inject ContextNamer contextNamer;
  @Bind(R.id.title) TextView title;
  @Bind(R.id.subtitle) TextView subTitle;
  @Bind(R.id.hello) Button button;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    Scope appScope = ToothPick.openScope(getApplication());
    Scope metaScope = ToothPick.openScope(PRESENTER_SCOPE);
    appScope.addChild(metaScope);
    scope = ToothPick.openScope(this);
    metaScope.addChild(scope);
    scope.installModules(new DefaultActivityModule(this));
    ToothPick.inject(this, scope);
    metaScope.installModules(new PresenterModule());

    setContentView(R.layout.simple_activity);
    ButterKnife.bind(this);
    title.setText("Persist");
    subTitle.setText(contextNamer.getInstanceCount());
    button.setVisibility(View.GONE);
  }

  @Override
  protected void onDestroy() {
    ToothPick.closeScope(this);
    super.onDestroy();
  }

  @Override
  public void onBackPressed() {
    ToothPick.closeScope(PRESENTER_SCOPE);
    super.onBackPressed();
  }

  private class PresenterModule extends Module {
    {
      bind(ContextNamer.class).to(contextNamer);
    }
  }
}