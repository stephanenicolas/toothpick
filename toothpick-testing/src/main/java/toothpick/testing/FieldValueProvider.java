package toothpick.testing;

import java.lang.reflect.Field;
import javax.inject.Provider;

import static java.lang.String.format;

class FieldValueProvider implements Provider {
  private final Field field;
  private final Object test;

  FieldValueProvider(Field field, Object test) {
    this.field = field;
    this.test = test;
    field.setAccessible(true);
  }

  @Override
  public Object get() {
    try {
      return field.get(test);
    } catch (IllegalAccessException e) {
      throw new RuntimeException(format("Impossible to get the value of the @Mock annotated field %s. This should not happen.", field.getName()));
    }
  }
}
