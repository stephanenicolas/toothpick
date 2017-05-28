package com.example.smoothie;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.example.smoothie.deps.RxPresenter;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import javax.inject.Inject;
import rx.Subscription;
import toothpick.Scope;
import toothpick.Toothpick;
import toothpick.smoothie.module.SmoothieActivityModule;

import static toothpick.Toothpick.openScopes;
import static toothpick.smoothie.module.Smoothie.autoCloseActivityScopeAndParentScope;

public class RxMVPActivity extends Activity {

  public static final Class PRESENTER_SCOPE = Presenter.class;
  private Scope scope;

  @Inject RxPresenter rxPresenter;
  @BindView(R.id.title) TextView title;
  @BindView(R.id.subtitle) TextView subTitle;
  @BindView(R.id.hello) Button button;
  private Subscription subscription;

  @Override protected void onCreate(Bundle savedInstanceState) {
    scope = openScopes(getApplication(), PRESENTER_SCOPE, this);
    scope.installModules(new SmoothieActivityModule(this));
    autoCloseActivityScopeAndParentScope(this);

    super.onCreate(savedInstanceState);
    Toothpick.inject(this, scope);

    setContentView(R.layout.simple_activity);
    ButterKnife.bind(this);
    title.setText("MVP");
    subscription = rxPresenter.subscribe(aLong -> subTitle.setText(String.valueOf(aLong)));
    button.setVisibility(View.GONE);
  }

  @Override
  protected void onDestroy() {
    subscription.unsubscribe();
    super.onDestroy();
  }

  @javax.inject.Scope @Target(ElementType.TYPE) @Retention(RetentionPolicy.RUNTIME)
  public @interface Presenter {
  }
}
