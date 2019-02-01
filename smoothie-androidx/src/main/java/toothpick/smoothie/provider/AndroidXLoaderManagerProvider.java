package toothpick.smoothie.provider;

import android.app.Activity;
import androidx.fragment.app.FragmentActivity;
import androidx.loader.app.LoaderManager;
import javax.inject.Provider;

public class AndroidXLoaderManagerProvider implements Provider<LoaderManager> {
  Activity activity;

  public AndroidXLoaderManagerProvider(Activity activity) {
    this.activity = activity;
  }

  @Override
  public LoaderManager get() {
    return ((FragmentActivity) activity).getSupportLoaderManager();
  }
}
