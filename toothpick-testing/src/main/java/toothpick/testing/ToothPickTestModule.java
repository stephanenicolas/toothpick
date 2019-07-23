/*
 * Copyright 2019 Stephane Nicolas
 * Copyright 2019 Daniel Molinero Reguera
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
      // works for Easymock, Mockito and MockK
      String annotationName = annotation.annotationType().getName();
      if (annotationName.contains("Mock") || annotationName.contains("Spy")) {
        return annotation;
      }
    }
    return null;
  }

  private String findInjectionName(Field field) {
    for (Annotation annotation : field.getAnnotations()) {
      // works for Easymock, Mockito and MockK
      if (annotation.annotationType() == Named.class) {
        return ((Named) annotation).value();
      }
      // works for Easymock, Mockito and MockK
      if (annotation.annotationType() != Inject.class) {
        String annotationName = annotation.annotationType().getName();
        if (!annotationName.contains("Mock") && !annotationName.contains("Spy")) {
          return annotation.annotationType().getCanonicalName();
        }
      }
    }
    return null;
  }

  public int getMockCount() {
    return mockCount;
  }
}
