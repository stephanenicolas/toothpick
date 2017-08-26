package toothpick.smoothie.provider;

import android.app.Application;
import android.content.res.Resources;
import javax.inject.Provider;

public class ResourcesProvider implements Provider<Resources> {
  Application application;

  public ResourcesProvider(Application application) {
    this.application = application;
  }

  @Override
  public Resources get() {
    return application.getResources();
  }
}
