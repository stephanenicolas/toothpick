package toothpick.smoothie.provider;

import android.app.Activity;
import android.app.FragmentManager;
import javax.inject.Provider;

public class FragmentManagerProvider implements Provider<FragmentManager> {
  Activity activity;

  public FragmentManagerProvider(Activity activity) {
    this.activity = activity;
  }

  @Override
  public FragmentManager get() {
    return activity.getFragmentManager();
  }
}
