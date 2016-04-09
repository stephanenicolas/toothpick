package toothpick.smoothie.provider;

import android.os.Handler;
import android.os.Looper;
import javax.inject.Inject;
import javax.inject.Provider;

public class HandlerProvider implements Provider<Handler> {
  @Inject
  public HandlerProvider() {
  }

  @Override
  public Handler get() {
    return new Handler(Looper.getMainLooper());
  }
}
