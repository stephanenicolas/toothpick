package toothpick.smoothie.provider;

import android.app.Activity;
import android.view.LayoutInflater;
import javax.inject.Provider;

public class LayoutInflaterProvider implements Provider<LayoutInflater> {
  Activity activity;

  public LayoutInflaterProvider(Activity activity) {
    this.activity = activity;
  }

  @Override
  public LayoutInflater get() {
    return LayoutInflater.from(activity);
  }
}
