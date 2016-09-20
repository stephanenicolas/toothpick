package toothpick.sample;

import javax.inject.Inject;
import toothpick.Scope;
import toothpick.Toothpick;

// https://github.com/stephanenicolas/toothpick/wiki/Suppressing-compilation-warnings
@SuppressWarnings("Injectable")
public class SecondEntryPoint {

  @Inject Computer computer;
  @Inject Computer2 computer2;

  private SecondEntryPoint() {
    Scope scope = Toothpick.openScope("SecondEntryPoint");
    Toothpick.inject(this, scope);
  }
}
