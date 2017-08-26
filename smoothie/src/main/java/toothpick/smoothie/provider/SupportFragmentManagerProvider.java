package toothpick.smoothie.provider;

import android.app.Activity;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import javax.inject.Provider;

public class SupportFragmentManagerProvider implements Provider<FragmentManager> {
  Activity activity;

  public SupportFragmentManagerProvider(Activity activity) {
    this.activity = activity;
  }

  @Override
  public FragmentManager get() {
    return ((FragmentActivity) activity).getSupportFragmentManager();
  }
}
