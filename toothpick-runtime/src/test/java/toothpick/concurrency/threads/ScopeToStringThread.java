package toothpick.concurrency.threads;

import java.lang.reflect.GenericDeclaration;
import java.security.Key;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.jar.JarEntry;
import javax.inject.Inject;
import toothpick.Lazy;
import toothpick.Scope;
import toothpick.ToothPick;
import toothpick.config.Module;
import toothpick.data.Foo;

public class ScopeToStringThread extends TestableThread {
  static int instanceNumber = 0;
  private Object scopeName;
  private static Random random = new Random();

  public ScopeToStringThread(Object scopeName) {
    super("ScopeToStringThread " + instanceNumber++);
    this.scopeName = scopeName;
  }

  @Override
  public void doRun() {
    ToothPick.openScope(scopeName).toString();
    setIsSuccessful(true);
  }
}
