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

public class SupportActivityModule extends Module {
  public SupportActivityModule(FragmentActivity activity) {
    bind(Activity.class).to(activity);
    bind(FragmentManager.class).toProvider(new SupportFragmentManagerProvider(activity));
    bind(LoaderManager.class).toProvider(new SupportLoaderManagerProvider(activity));
    bind(LayoutInflater.class).toProvider(new LayoutInflaterProvider(activity));
  }
}
