package toothpick.configuration;

/**
 * Thrown when a binding is illegal.
 */
public class IllegalBindingException extends IllegalStateException {
  public IllegalBindingException() {
  }

  public IllegalBindingException(String message) {
    super(message);
  }

  public IllegalBindingException(String message, Throwable cause) {
    super(message, cause);
  }

  public IllegalBindingException(Throwable cause) {
    super(cause);
  }
}
