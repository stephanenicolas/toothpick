package toothpick.smoothie.provider;

import android.app.Activity;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import javax.inject.Provider;

public class AndroidXFragmentManagerProvider implements Provider<FragmentManager> {
  Activity activity;

  public AndroidXFragmentManagerProvider(Activity activity) {
    this.activity = activity;
  }

  @Override
  public FragmentManager get() {
    return ((FragmentActivity) activity).getSupportFragmentManager();
  }
}
