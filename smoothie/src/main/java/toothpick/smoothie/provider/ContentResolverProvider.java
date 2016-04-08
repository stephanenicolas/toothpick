package toothpick.smoothie.provider;

import android.content.ContentResolver;
import android.content.Context;
import javax.inject.Inject;
import javax.inject.Provider;

public class ContentResolverProvider implements Provider<ContentResolver> {
  @Inject Context context;

  @Override public ContentResolver get() {
    return context.getContentResolver();
  }
}
