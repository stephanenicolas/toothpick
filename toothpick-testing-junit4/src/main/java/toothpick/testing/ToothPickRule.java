package toothpick.testing;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import toothpick.Scope;
import toothpick.Toothpick;

public class ToothPickRule implements TestRule {
  private ToothPickTestModule testModule;
  private Scope scope;

  public ToothPickRule(Object test) {
    this(test, null);
  }

  public ToothPickRule(Object test, Object scopeName) {
    this.testModule = new ToothPickTestModule(test);
    if (scopeName != null) {
      setScopeName(scopeName);
    }
  }

  public void setScopeName(Object scopeName) {
    if (scope != null) {
      throw new IllegalStateException("scope is already initialized, use a constructor without a scope name for the rule.");
    }
    scope = Toothpick.openScope(scopeName);
    scope.installTestModules(testModule);
  }

  @Override
  public Statement apply(Statement base, Description description) {
    return new ToothPickStatement(base);
  }

  public ToothPickTestModule getTestModule() {
    return testModule;
  }

  public Scope getScope() {
    return scope;
  }

  public void inject(Object objectUnderTest) {
    Toothpick.inject(objectUnderTest, scope);
  }

  public <T> T getInstance(Class<T> clazz) {
    return getInstance(clazz, null);
  }

  public <T> T getInstance(Class<T> clazz, String name) {
    return scope.getInstance(clazz, name);
  }

}
