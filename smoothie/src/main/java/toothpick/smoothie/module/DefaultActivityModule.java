package toothpick.smoothie.module;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.LoaderManager;
import toothpick.config.Module;
import toothpick.smoothie.provider.FragmentManagerProvider;
import toothpick.smoothie.provider.LoaderManagerProvider;

public class DefaultActivityModule extends Module {
  public DefaultActivityModule(Activity activity) {
    bind(Activity.class).to(activity);
    bind(FragmentManager.class).toProvider(FragmentManagerProvider.class);
    bind(LoaderManager.class).toProvider(LoaderManagerProvider.class);
  }
}
