package toothpick.smoothie.provider;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import javax.inject.Inject;
import javax.inject.Provider;

// TODO only pass default shared preferences?
public class SharedPreferencesProvider implements Provider<SharedPreferences> {
  @Inject Context context;

  @Override public SharedPreferences get() {
    return PreferenceManager.getDefaultSharedPreferences(context);
  }
}
