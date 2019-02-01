package toothpick.sample;

import org.easymock.EasyMockSupport;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

public class EasyMockExtension implements BeforeEachCallback {

  private final Object test;

  public EasyMockExtension(Object test) {
    this.test = test;
  }

  @Override
  public void beforeEach(ExtensionContext context) throws Exception {
    EasyMockSupport.injectMocks(test);
  }
}
