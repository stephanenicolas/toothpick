package toothpick.testing;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import javax.inject.Inject;
import javax.inject.Named;
import toothpick.config.Module;

public class ToothPickTestModule extends Module {

  private Object test;
  private int mockCount;

  /**
   * Automatically binds all annotated mocks of a module.
   *
   * @param test the test whose {@code Mock} annotated fields will be mocked.
   */
  public ToothPickTestModule(Object test) {
    bindAllMocks(test);
  }

  /**
   * Bind all {@code Mock} annotated field of a given test.
   *
   * @param test the test whose fields are going to be injected.
   */
  @SuppressWarnings("unchecked")
  public void bindAllMocks(Object test) {
    mockCount = 0;
    for (Field field : test.getClass().getDeclaredFields()) {
      Annotation mockAnnotation = findMockAnnotation(field);
      String injectionName = findInjectionName(field);
      if (mockAnnotation != null) {
        FieldValueProvider mockProvider = new FieldValueProvider(field, test);
        if (injectionName != null) {
          bind(field.getType()).withName(injectionName).toProviderInstance(mockProvider);
        } else {
          bind(field.getType()).toProviderInstance(mockProvider);
        }
        mockCount++;
      }
    }
  }

  private Annotation findMockAnnotation(Field field) {
    for (Annotation annotation : field.getAnnotations()) {
      //works for both easy mock and mockito
      if (annotation.annotationType().getName().contains("Mock")) {
        return annotation;
      }
    }
    return null;
  }

  private String findInjectionName(Field field) {
    for (Annotation annotation : field.getAnnotations()) {
      //works for both easy mock and mockito
      if (annotation.annotationType() == Named.class) {
        return ((Named) annotation).value();
      }
      //works for both easy mock and mockito
      if (annotation.annotationType() != Inject.class && !annotation.annotationType().getName().contains("Mock")) {
        return annotation.annotationType().getName();
      }
    }
    return null;
  }

  public int getMockCount() {
    return mockCount;
  }
}
