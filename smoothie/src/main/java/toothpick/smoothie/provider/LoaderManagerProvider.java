package toothpick.smoothie.provider;

import android.app.Activity;
import android.app.LoaderManager;
import javax.inject.Inject;
import javax.inject.Provider;

public class LoaderManagerProvider implements Provider<LoaderManager> {
  @Inject Activity activity;

  @Override public LoaderManager get() {
    return activity.getLoaderManager();
  }
}
