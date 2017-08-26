package toothpick.smoothie.provider;

import android.accounts.AccountManager;
import android.app.Application;
import javax.inject.Provider;

public class AccountManagerProvider implements Provider<AccountManager> {
  Application application;

  public AccountManagerProvider(Application application) {
    this.application = application;
  }

  @Override
  public AccountManager get() {
    return AccountManager.get(application);
  }
}
