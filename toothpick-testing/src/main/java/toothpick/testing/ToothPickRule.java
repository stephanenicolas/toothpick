package toothpick.testing;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import toothpick.Scope;
import toothpick.ToothPick;
import toothpick.registries.FactoryRegistry;
import toothpick.registries.MemberInjectorRegistry;
import toothpick.registries.factory.FactoryRegistryLocator;
import toothpick.registries.memberinjector.MemberInjectorRegistryLocator;

public class ToothPickRule implements TestRule {
  private ToothPickTestModule testModule;
  private Scope scope;

  public ToothPickRule(Object test) {
    this(test, null);
    try {
      throw new RuntimeException();
    } catch (RuntimeException e) {
      e.printStackTrace();
    }
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
    scope = ToothPick.openScope(scopeName);
    scope.installTestModules(testModule);
  }

  @Override
  public Statement apply(Statement base, Description description) {
    try {
      return base;
    } finally {
      ToothPick.reset();
    }
  }

  public ToothPickTestModule getTestModule() {
    return testModule;
  }

  public Scope getScope() {
    return scope;
  }

  public void inject(Object objectUnderTest) {
    ToothPick.inject(objectUnderTest, scope);
  }

  public ToothPickRule setRootRegistryPackage(String rootRegistryPackageName) {
    try {
      Class factoryRegistryClass = Class.forName(rootRegistryPackageName + ".FactoryRegistry");
      Class memberInjectorRegistryClass = Class.forName(rootRegistryPackageName + ".MemberInjectorRegistry");
      FactoryRegistryLocator.setRootRegistry((FactoryRegistry) factoryRegistryClass.newInstance());
      MemberInjectorRegistryLocator.setRootRegistry((MemberInjectorRegistry) memberInjectorRegistryClass.newInstance());
      return this;
    } catch (Exception e) {
      throw new IllegalArgumentException("Invalid package to find registries : " + rootRegistryPackageName, e);
    }
  }

  public <T> T getInstance(Class<T> clazz) {
    return scope.getInstance(clazz);
  }
}
