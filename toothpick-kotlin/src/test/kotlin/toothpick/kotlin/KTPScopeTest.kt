package toothpick.kotlin

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

        // WHEN
        val result = ktpScope.parentScope

        // THEN
        Verify on childScope that childScope.parentScope was called
        result shouldBeInstanceOf KTPScope::class
    }

    @Test
    fun `getParentScope with annotation should return wrapped parent scope of to the provided scope`() {
        // GIVEN
        val childScope: Scope = mock()
        val parentScope: Scope = mock()
        val ktpScope = KTPScope(childScope)

        When calling childScope.getParentScope(Singleton::class.java) itReturns parentScope

        // WHEN
        val result = ktpScope.getParentScope(Singleton::class.java)

        // THEN
        Verify on childScope that childScope.getParentScope(Singleton::class.java) was called
        result shouldBeInstanceOf KTPScope::class
    }

    @Test
    fun `getRootScope should return wrapped root scope of to the provided scope`() {
        // GIVEN
        val childScope: Scope = mock()
        val rootScope: Scope = mock()
        val ktpScope = KTPScope(childScope)

        When calling childScope.rootScope itReturns rootScope

        // WHEN
        val result = ktpScope.rootScope

        // THEN
        Verify on childScope that childScope.rootScope was called
        result shouldBeInstanceOf KTPScope::class
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
