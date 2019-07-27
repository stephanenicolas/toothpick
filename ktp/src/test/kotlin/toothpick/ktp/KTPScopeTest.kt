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
import org.amshove.kluent.Verify
import org.amshove.kluent.When
import org.amshove.kluent.called
import org.amshove.kluent.calling
import org.amshove.kluent.itReturns
import org.amshove.kluent.on
import org.amshove.kluent.shouldBe
import org.amshove.kluent.shouldBeInstanceOf
import org.amshove.kluent.shouldEqual
import org.amshove.kluent.that
import org.amshove.kluent.was
import org.junit.jupiter.api.Test
import toothpick.Scope
import toothpick.config.Module
import javax.inject.Singleton

class KTPScopeTest {

    @Test
    fun `installModules should install modules on the provided scope`() {
        // GIVEN
        val module = Module()
        val scope: Scope = mock()
        val ktpScope = KTPScope(scope)

        // WHEN
        val result = ktpScope.installModules(module)

        // THEN
        Verify on scope that scope.installModules(module) was called
        result shouldBe ktpScope
    }

    @Test
    fun `supportScopeAnnotation should delegate to the provided scope`() {
        // GIVEN
        val scope: Scope = mock()
        val ktpScope = KTPScope(scope)

        // WHEN
        val result = ktpScope.supportScopeAnnotation(Singleton::class.java)

        // THEN
        Verify on scope that scope.supportScopeAnnotation(Singleton::class.java) was called
        result shouldBe ktpScope
    }

    @Test
    fun `getParentScope should return wrapped parent scope of to the provided scope`() {
        // GIVEN
        val childScope: Scope = mock()
        val parentScope: Scope = mock()
        val ktpScope = KTPScope(childScope)

        When calling childScope.parentScope itReturns parentScope
        When calling parentScope.name itReturns "parent"

        // WHEN
        val result = ktpScope.parentScope

        // THEN
        Verify on childScope that childScope.parentScope was called
        result shouldBeInstanceOf KTPScope::class
        result.name shouldEqual "parent"
    }

    @Test
    fun `getParentScope with annotation should return wrapped parent scope of to the provided scope`() {
        // GIVEN
        val childScope: Scope = mock()
        val parentScope: Scope = mock()
        val ktpScope = KTPScope(childScope)

        When calling childScope.getParentScope(Singleton::class.java) itReturns parentScope
        When calling parentScope.name itReturns "parent"

        // WHEN
        val result = ktpScope.getParentScope(Singleton::class.java)

        // THEN
        Verify on childScope that childScope.getParentScope(Singleton::class.java) was called
        result shouldBeInstanceOf KTPScope::class
        result.name shouldEqual "parent"
    }

    @Test
    fun `getRootScope should return wrapped root scope of to the provided scope`() {
        // GIVEN
        val childScope: Scope = mock()
        val rootScope: Scope = mock()
        val ktpScope = KTPScope(childScope)

        When calling childScope.rootScope itReturns rootScope
        When calling rootScope.name itReturns "root"

        // WHEN
        val result = ktpScope.rootScope

        // THEN
        Verify on childScope that childScope.rootScope was called
        result shouldBeInstanceOf KTPScope::class
        result.name shouldEqual "root"
    }

    @Test
    fun `openSubScope should return wrapped sub scope of the provided scope`() {
        // GIVEN
        val childScope: Scope = mock()
        val parentScope: Scope = mock()
        val ktpScope = KTPScope(parentScope)

        When calling parentScope.openSubScope("name") itReturns childScope
        When calling childScope.name itReturns "child"

        // WHEN
        val result = ktpScope.openSubScope("name")

        // THEN
        Verify on parentScope that parentScope.openSubScope("name") was called
        result shouldBeInstanceOf KTPScope::class
        result.name shouldEqual "child"
    }

    @Test
    fun `openSubScope should return wrapped sub scope of the provided scope and provide ScopeConfig`() {
        // GIVEN
        val childScope: Scope = mock()
        val parentScope: Scope = mock()
        val config: Scope.ScopeConfig = mock()
        val ktpScope = KTPScope(parentScope)

        When calling parentScope.openSubScope("name", config) itReturns childScope
        When calling parentScope.name itReturns "root"
        When calling childScope.name itReturns "child"

        // WHEN
        val result = ktpScope.openSubScope("name", config)

        // THEN
        Verify on parentScope that parentScope.openSubScope("name", config) was called
        result shouldBeInstanceOf KTPScope::class
        result.name shouldEqual "child"
    }

    @Test
    fun `getInstance should return instance without name using the provided scope`() {
        // GIVEN
        val scope: Scope = mock()
        val ktpScope = KTPScope(scope)

        When calling scope.getInstance(String::class.java, null) itReturns "test"

        // WHEN
        val result: String = ktpScope.getInstance()

        // THEN
        Verify on scope that scope.getInstance(String::class.java, null) was called
        result shouldEqual "test"
    }

    @Test
    fun `getInstance should return instance with name using the provided scope`() {
        // GIVEN
        val scope: Scope = mock()
        val ktpScope = KTPScope(scope)

        When calling scope.getInstance(String::class.java, "name") itReturns "test"

        // WHEN
        val result: String = ktpScope.getInstance("name")

        // THEN
        Verify on scope that scope.getInstance(String::class.java, "name") was called
        result shouldEqual "test"
    }
}
