package toothpick.smoothie.provider;

import android.os.Handler;
import android.os.Looper;
import javax.inject.Provider;

public class HandlerProvider implements Provider<Handler> {
  public HandlerProvider() {
  }

  @Override
  public Handler get() {
    return new Handler(Looper.getMainLooper());
  }
}
