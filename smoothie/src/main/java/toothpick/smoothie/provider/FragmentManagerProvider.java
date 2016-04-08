package toothpick.smoothie.provider;

import android.app.Activity;
import android.app.FragmentManager;
import javax.inject.Inject;
import javax.inject.Provider;

public class FragmentManagerProvider implements Provider<FragmentManager> {
  @Inject Activity activity;

  @Override public FragmentManager get() {
    return activity.getFragmentManager();
  }
}
