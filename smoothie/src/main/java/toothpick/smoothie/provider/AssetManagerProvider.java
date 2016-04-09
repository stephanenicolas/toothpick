package toothpick.smoothie.provider;

import android.app.Application;
import android.content.res.AssetManager;
import javax.inject.Inject;
import javax.inject.Provider;

public class AssetManagerProvider implements Provider<AssetManager> {
  Application application;

  @Inject
  public AssetManagerProvider(Application application) {
    this.application = application;
  }

  @Override
  public AssetManager get() {
    return application.getAssets();
  }
}
