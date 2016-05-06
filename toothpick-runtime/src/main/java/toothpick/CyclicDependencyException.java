package toothpick;

public class CyclicDependencyException extends RuntimeException {
  public CyclicDependencyException() {
  }

  public CyclicDependencyException(String message) {
    super(message);
  }

  public CyclicDependencyException(String message, Throwable cause) {
    super(message, cause);
  }

  public CyclicDependencyException(Throwable cause) {
    super(cause);
  }

  public CyclicDependencyException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }
}
