package com.example.smoothie;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.example.smoothie.deps.RxPresenter;
import javax.inject.Inject;
import rx.Subscription;
import toothpick.Scope;
import toothpick.ToothPick;
import toothpick.config.Module;
import toothpick.smoothie.module.ActivityModule;

public class RxMVPActivity extends Activity {

  public static final String PRESENTER_SCOPE = "PRESENTER_SCOPE";
  private Scope scope;

  @Inject RxPresenter rxPresenter;
  @BindView(R.id.title) TextView title;
  @BindView(R.id.subtitle) TextView subTitle;
  @BindView(R.id.hello) Button button;
  private Subscription subscription;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    scope = ToothPick.openScopes(getApplication(), PRESENTER_SCOPE, this);
    scope.installModules(new ActivityModule(this));
    ToothPick.inject(this, scope);
    ToothPick.openScope(PRESENTER_SCOPE).installModules(new PresenterModule());

    setContentView(R.layout.simple_activity);
    ButterKnife.bind(this);
    title.setText("MVP");
    subscription = rxPresenter.subscribe(aLong -> subTitle.setText(String.valueOf(aLong)));
    button.setVisibility(View.GONE);
  }

  @Override
  protected void onDestroy() {
    ToothPick.closeScope(this);
    subscription.unsubscribe();
    super.onDestroy();
  }

  @Override
  public void onBackPressed() {
    //when we leave the presenter flow,
    //we close its scope
    rxPresenter.stop();
    ToothPick.closeScope(PRESENTER_SCOPE);
    super.onBackPressed();
  }

  private class PresenterModule extends Module {
    {
      bind(RxPresenter.class).to(rxPresenter);
    }
  }
}