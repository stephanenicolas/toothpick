package toothpick.smoothie.module;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.LoaderManager;
import android.view.LayoutInflater;
import toothpick.config.Module;
import toothpick.smoothie.provider.FragmentManagerProvider;
import toothpick.smoothie.provider.LayoutInflaterProvider;
import toothpick.smoothie.provider.LoaderManagerProvider;

public class ActivityModule extends Module {
  public ActivityModule(Activity activity) {
    bind(Activity.class).to(activity);
    bind(FragmentManager.class).toProvider(new FragmentManagerProvider(activity));
    bind(LoaderManager.class).toProvider(new LoaderManagerProvider(activity));
    bind(LayoutInflater.class).toProvider(new LayoutInflaterProvider(activity));
  }
}
