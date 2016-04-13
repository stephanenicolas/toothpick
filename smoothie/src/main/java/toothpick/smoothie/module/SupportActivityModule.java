package toothpick.smoothie.module;

import android.app.Activity;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.LoaderManager;
import toothpick.config.Module;
import toothpick.smoothie.provider.SupportFragmentManagerProvider;
import toothpick.smoothie.provider.SupportLoaderManagerProvider;

public class SupportActivityModule extends Module {
  public SupportActivityModule(FragmentActivity activity) {
    bind(Activity.class).to(activity);
    bind(FragmentManager.class).toProvider(SupportFragmentManagerProvider.class);
    bind(LoaderManager.class).toProvider(SupportLoaderManagerProvider.class);
  }
}
