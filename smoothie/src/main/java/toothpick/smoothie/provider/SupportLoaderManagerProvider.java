package toothpick.smoothie.provider;

import android.app.Activity;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager;
import javax.inject.Provider;

public class SupportLoaderManagerProvider implements Provider<LoaderManager> {
  Activity activity;

  public SupportLoaderManagerProvider(Activity activity) {
    this.activity = activity;
  }

  @Override
  public LoaderManager get() {
    return ((FragmentActivity) activity).getSupportLoaderManager();
  }
}
