package toothpick.smoothie.provider;

import android.content.Context;
import android.content.res.AssetManager;
import javax.inject.Inject;
import javax.inject.Provider;

public class AssetManagerProvider implements Provider<AssetManager> {
  @Inject Context context;

  @Override public AssetManager get() {
    return context.getAssets();
  }
}
