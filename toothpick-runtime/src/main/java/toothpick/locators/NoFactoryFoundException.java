package toothpick.locators;

import static java.lang.String.format;

public class NoFactoryFoundException extends RuntimeException {
  public NoFactoryFoundException(Class clazz) {
    this(clazz, null);
  }

  public NoFactoryFoundException(Class clazz, Throwable cause) {
    super(format("No factory could be found for class %s. " //
        + "Check that the class has either a @Inject annotated constructor " //
        + "or contains @Inject annotated members.", clazz.getName()), cause);
  }
}
