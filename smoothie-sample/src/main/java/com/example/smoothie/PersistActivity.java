package com.example.smoothie;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.example.smoothie.deps.ContextNamer;
import javax.inject.Inject;
import toothpick.Scope;
import toothpick.ToothPick;
import toothpick.config.Module;
import toothpick.smoothie.module.ActivityModule;

public class PersistActivity extends Activity {

  public static final String PRESENTER_SCOPE = "PRESENTER_SCOPE";
  private Scope scope;

  @Inject ContextNamer contextNamer;
  @BindView(R.id.title) TextView title;
  @BindView(R.id.subtitle) TextView subTitle;
  @BindView(R.id.hello) Button button;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    //Smoothie.openApplicationScope(getApplication(), <modules...>) no need to include application module
    //.openPresenterScope(<modules...>) no need to include activity module
    //.openActivityScope(this, <modules...>) no need to include activity module
    //.inject(this) // all DSL scope state can inject
    //.parentScope() // all DSL scope state can provide a parent or root
    //.add(contextNamer); // all DSL scope state can add

    scope = ToothPick.openScopes(getApplication(), PRESENTER_SCOPE, this);
    scope.installModules(new ActivityModule(this));
    ToothPick.inject(this, scope);
    ToothPick.openScope(PRESENTER_SCOPE).installModules(new PresenterModule());

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
    //when we leave the presenter flow,
    //we close its scope
    ToothPick.closeScope(PRESENTER_SCOPE);
    super.onBackPressed();
  }

  private class PresenterModule extends Module {
    {
      bind(ContextNamer.class).to(contextNamer);
    }
  }
}