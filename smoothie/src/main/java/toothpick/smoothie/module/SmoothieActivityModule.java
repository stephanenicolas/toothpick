package toothpick.smoothie.module;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.LoaderManager;
import android.view.LayoutInflater;
import toothpick.config.Module;
import toothpick.smoothie.provider.FragmentManagerProvider;
import toothpick.smoothie.provider.LayoutInflaterProvider;
import toothpick.smoothie.provider.LoaderManagerProvider;

public class SmoothieActivityModule extends Module {
  public SmoothieActivityModule(Activity activity) {
    bind(Activity.class).toInstance(activity);
    bind(FragmentManager.class).toProviderInstance(new FragmentManagerProvider(activity));
    bind(LoaderManager.class).toProviderInstance(new LoaderManagerProvider(activity));
    bind(LayoutInflater.class).toProviderInstance(new LayoutInflaterProvider(activity));
  }
}
