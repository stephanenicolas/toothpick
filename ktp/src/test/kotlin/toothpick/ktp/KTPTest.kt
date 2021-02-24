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
package toothpick.ktp

import com.nhaarman.mockitokotlin2.mock
import org.amshove.kluent.shouldBe
import org.amshove.kluent.shouldBeFalse
import org.amshove.kluent.shouldBeTrue
import org.amshove.kluent.shouldEqual
import org.amshove.kluent.shouldNotBe
import org.amshove.kluent.shouldNotBeNull
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import toothpick.Toothpick
import toothpick.configuration.Configuration
import toothpick.configuration.ConfigurationHolder

class KTPTest {

    @AfterEach
    fun tearDown() {
        KTP.reset()
    }

    @Test
    fun `openRootScope should open default root scope`() {
        // GIVEN
        KTP.isRootScopeOpen().shouldBeFalse()

        // WHEN
        val scope = KTP.openRootScope()

        // THEN
        scope.shouldNotBeNull()
        KTP.isRootScopeOpen().shouldBeTrue()
    }

    @Test
    fun `openRootScope should open default root scope with a scope config`() {
        // GIVEN
        var configApplied = false

        // WHEN
        val scope = KTP.openRootScope { configApplied = true }

        // THEN
        scope.shouldNotBeNull()
        KTP.isRootScopeOpen().shouldBeTrue()
        configApplied.shouldBeTrue()
    }

    @Test
    fun `openScope should open scope`() {
        // GIVEN
        Toothpick.isScopeOpen("name").shouldBeFalse()

        // WHEN
        val scope = KTP.openScope("name")

        // THEN
        scope.name shouldEqual "name"
        Toothpick.isScopeOpen("name").shouldBeTrue()
        KTP.isScopeOpen("name").shouldBeTrue()
    }

    @Test
    fun `openScope should open scope with a scope config`() {
        // GIVEN
        Toothpick.isScopeOpen("name").shouldBeFalse()
        var configApplied = false

        // WHEN
        val scope = KTP.openScope("name") { configApplied = true }

        // THEN
        scope.name shouldEqual "name"
        Toothpick.isScopeOpen("name").shouldBeTrue()
        KTP.isScopeOpen("name").shouldBeTrue()
        configApplied.shouldBeTrue()
    }

    @Test
    fun `openScopes should open scopes and child scope`() {
        // GIVEN
        Toothpick.isScopeOpen("parent").shouldBeFalse()
        Toothpick.isScopeOpen("child").shouldBeFalse()

        // WHEN
        val scope = KTP.openScopes("parent", "child")

        // THEN
        scope.name shouldEqual "child"
        Toothpick.isScopeOpen("parent").shouldBeTrue()
        KTP.isScopeOpen("parent").shouldBeTrue()
        Toothpick.isScopeOpen("child").shouldBeTrue()
        KTP.isScopeOpen("child").shouldBeTrue()
    }

    @Test
    fun `closeScope should close scope`() {
        // GIVEN
        Toothpick.openScope("name")
        Toothpick.isScopeOpen("name").shouldBeTrue()

        // WHEN
        KTP.closeScope("name")

        // THEN
        Toothpick.isScopeOpen("name").shouldBeFalse()
    }

    @Test
    fun `isScopeOpen should return true when scope is open`() {
        // GIVEN
        Toothpick.openScope("name")
        Toothpick.isScopeOpen("name").shouldBeTrue()

        // THEN
        KTP.isScopeOpen("name").shouldBeTrue()
    }

    @Test
    fun `isScopeOpen should return false when scope is closed`() {
        // GIVEN
        Toothpick.isScopeOpen("name").shouldBeFalse()

        // THEN
        KTP.isScopeOpen("name").shouldBeFalse()
    }

    @Test
    fun `setConfiguration should set the configuration`() {
        // GIVEN
        val configuration: Configuration = mock()
        ConfigurationHolder.configuration shouldNotBe configuration

        // WHEN
        KTP.setConfiguration(configuration)

        // THEN
        ConfigurationHolder.configuration shouldBe configuration
    }
}
