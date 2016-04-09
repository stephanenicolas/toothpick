package toothpick.smoothie.provider;

import android.app.Activity;
import android.app.LoaderManager;
import javax.inject.Inject;
import javax.inject.Provider;

public class LoaderManagerProvider implements Provider<LoaderManager> {
  Activity activity;

  @Inject
  public LoaderManagerProvider(Activity activity) {
    this.activity = activity;
  }

  @Override
  public LoaderManager get() {
    return activity.getLoaderManager();
  }
}
