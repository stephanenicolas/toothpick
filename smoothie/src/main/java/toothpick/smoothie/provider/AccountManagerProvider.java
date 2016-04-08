package toothpick.smoothie.provider;

import android.accounts.AccountManager;
import android.content.Context;
import javax.inject.Inject;
import javax.inject.Provider;

public class AccountManagerProvider implements Provider<AccountManager> {
  @Inject Context context;

  @Override public AccountManager get() {
    return AccountManager.get(context);
  }
}
