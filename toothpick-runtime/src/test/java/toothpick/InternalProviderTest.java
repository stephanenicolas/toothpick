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
package toothpick;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import javax.inject.Provider;
import org.junit.Rule;
import org.junit.Test;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.rule.PowerMockRule;
import toothpick.locators.FactoryLocator;

@PrepareForTest(FactoryLocator.class)
public class InternalProviderTest {

  public @Rule PowerMockRule rule = new PowerMockRule();

  @Test(expected = IllegalArgumentException.class)
  public void testCreateInternalProviderImpl_shouldFail_whenInstanceIsNull() {
    // GIVEN
    // WHEN
    new InternalProvider<>((String) null);

    // THEN
    fail("Should throw an exception");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testCreateInternalProviderImpl_shouldFail_whenProviderInstanceIsNull() {
    // GIVEN
    // WHEN
    new InternalProvider<>((Provider<String>) null, false, false);

    // THEN
    fail("Should throw an exception");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testCreateInternalProviderImpl_shouldFail_whenFactoryIsNull() {
    // GIVEN
    // WHEN
    new InternalProvider<>((Factory<String>) null);

    // THEN
    fail("Should throw an exception");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testCreateInternalProviderImpl_shouldFail_whenFactoryClassIsNull() {
    // GIVEN
    // WHEN
    new InternalProvider<>((Class<Void>) null, false, false);

    // THEN
    fail("Should throw an exception");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testCreateInternalProviderImpl_shouldFail_whenProviderFactoryClassIsNull() {
    // GIVEN
    // WHEN
    new InternalProvider<>(null, false, false, false, false);

    // THEN
    fail("Should throw an exception");
  }

  @Test
  @SuppressWarnings("unchecked")
  public void testGet_releasableFactorySingleton_afterRelease() {
    Factory<String> mockFactory = mock(Factory.class);
    when(mockFactory.hasSingletonAnnotation()).thenReturn(true);
    when(mockFactory.hasReleasableAnnotation()).thenReturn(true);
    when(mockFactory.createInstance(any(Scope.class))).thenReturn("hello world");

    Scope mockScope = mock(Scope.class);

    InternalProvider<String> provider = new InternalProvider<>(mockFactory);

    assertEquals("hello world", provider.get(mockScope));
    provider.release();
    assertEquals("hello world", provider.get(mockScope));
  }

  @Test
  @SuppressWarnings("unchecked")
  public void testGet_releasableProviderFactorySingleton_afterRelease() {
    Provider<String> mockProvider = mock(Provider.class);
    when(mockProvider.get()).thenReturn("hello world");

    Factory<Provider<String>> mockFactory = mock(Factory.class);
    when(mockFactory.createInstance(any(Scope.class))).thenReturn(mockProvider);

    PowerMockito.mockStatic(FactoryLocator.class);
    PowerMockito.when(FactoryLocator.getFactory(any(Class.class))).thenReturn(mockFactory);

    Scope mockScope = mock(Scope.class);

    InternalProvider provider = new InternalProvider(Provider.class, true, true, false, false);

    assertEquals("hello world", provider.get(mockScope));
    provider.release();
    assertEquals("hello world", provider.get(mockScope));
  }

  @Test
  @SuppressWarnings("unchecked")
  public void testGet_releasableProviderFactoryProvidingSingleton_afterRelease() {
    Provider<String> mockProvider = mock(Provider.class);
    when(mockProvider.get()).thenReturn("hello world");

    Factory<Provider<String>> mockFactory = mock(Factory.class);
    when(mockFactory.createInstance(any(Scope.class))).thenReturn(mockProvider);

    PowerMockito.mockStatic(FactoryLocator.class);
    PowerMockito.when(FactoryLocator.getFactory(any(Class.class))).thenReturn(mockFactory);

    Scope mockScope = mock(Scope.class);

    InternalProvider provider = new InternalProvider(Provider.class, false, false, true, true);

    assertEquals("hello world", provider.get(mockScope));
    provider.release();
    assertEquals("hello world", provider.get(mockScope));
  }

  /* TODO we should have unit tests for this
  @Test
  public void testGet() throws Exception {

  }
  */
}
