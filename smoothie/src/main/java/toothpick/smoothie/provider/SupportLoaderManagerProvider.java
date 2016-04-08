package toothpick.smoothie.provider;

import android.app.Activity;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager;
import javax.inject.Inject;
import javax.inject.Provider;

public class SupportLoaderManagerProvider implements Provider<LoaderManager> {
  @Inject Activity activity;

  @Override public LoaderManager get() {
    return ((FragmentActivity) activity).getSupportLoaderManager();
  }
}
