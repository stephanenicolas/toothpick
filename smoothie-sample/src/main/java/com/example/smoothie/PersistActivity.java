package com.example.smoothie;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.example.smoothie.deps.PresenterContextNamer;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import javax.inject.Inject;
import toothpick.Scope;
import toothpick.ToothPick;
import toothpick.smoothie.module.ActivityModule;

public class PersistActivity extends Activity {

  public static final Class PRESENTER_SCOPE = Presenter.class;
  private Scope scope;

  @Inject PresenterContextNamer contextNamer;
  @BindView(R.id.title) TextView title;
  @BindView(R.id.subtitle) TextView subTitle;
  @BindView(R.id.hello) Button button;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    scope = ToothPick.openScopes(getApplication(), PRESENTER_SCOPE, this);
    scope.installModules(new ActivityModule(this));
    ToothPick.inject(this, scope);

    setContentView(R.layout.simple_activity);
    ButterKnife.bind(this);
    title.setText("MVP");
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

  @javax.inject.Scope
  @Target(ElementType.TYPE)
  @Retention(RetentionPolicy.RUNTIME)
  public @interface Presenter {

  }
}