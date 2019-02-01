package toothpick.smoothie.module;

import android.app.Activity;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.loader.app.LoaderManager;
import android.view.LayoutInflater;
import toothpick.config.Module;
import toothpick.smoothie.provider.AndroidXFragmentManagerProvider;
import toothpick.smoothie.provider.AndroidXLoaderManagerProvider;
import toothpick.smoothie.provider.LayoutInflaterProvider;

public class SmoothieAndroidXActivityModule extends Module {
  public SmoothieAndroidXActivityModule(FragmentActivity activity) {
    bind(Activity.class).toInstance(activity);
    bind(FragmentManager.class).toProviderInstance(new AndroidXFragmentManagerProvider(activity));
    bind(LoaderManager.class).toProviderInstance(new AndroidXLoaderManagerProvider(activity));
    bind(LayoutInflater.class).toProviderInstance(new LayoutInflaterProvider(activity));
  }
}
