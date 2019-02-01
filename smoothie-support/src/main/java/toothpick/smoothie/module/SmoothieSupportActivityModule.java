package toothpick.smoothie.module;

import android.app.Activity;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.LoaderManager;
import android.view.LayoutInflater;
import toothpick.config.Module;
import toothpick.smoothie.provider.LayoutInflaterProvider;
import toothpick.smoothie.provider.SupportFragmentManagerProvider;
import toothpick.smoothie.provider.SupportLoaderManagerProvider;

public class SmoothieSupportActivityModule extends Module {
  public SmoothieSupportActivityModule(FragmentActivity activity) {
    bind(Activity.class).toInstance(activity);
    bind(FragmentManager.class).toProviderInstance(new SupportFragmentManagerProvider(activity));
    bind(LoaderManager.class).toProviderInstance(new SupportLoaderManagerProvider(activity));
    bind(LayoutInflater.class).toProviderInstance(new LayoutInflaterProvider(activity));
  }
}
