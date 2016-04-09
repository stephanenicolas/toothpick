package toothpick.smoothie.provider;

import android.content.Context;
import javax.inject.Provider;

public class SystemServiceProvider<T> implements Provider<T> {
  private Context context;
  private String serviceName;

  public SystemServiceProvider(Context context, String serviceName) {
    this.context = context;
    this.serviceName = serviceName;
  }

  @SuppressWarnings("unchecked")
  @Override
  public T get() {
    return (T) context.getSystemService(serviceName);
  }
}
