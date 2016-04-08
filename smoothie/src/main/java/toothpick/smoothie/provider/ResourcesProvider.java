package toothpick.smoothie.provider;

import android.content.Context;
import android.content.res.Resources;
import javax.inject.Inject;
import javax.inject.Provider;

public class ResourcesProvider implements Provider<Resources> {
  @Inject Context context;

  @Override public Resources get() {
    return context.getResources();
  }
}
