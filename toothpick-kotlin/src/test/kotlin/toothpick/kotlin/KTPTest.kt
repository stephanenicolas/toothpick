package toothpick.kotlin

import com.nhaarman.mockitokotlin2.mock
import org.amshove.kluent.shouldBe
import org.amshove.kluent.shouldBeFalse
import org.amshove.kluent.shouldBeInstanceOf
import org.amshove.kluent.shouldBeTrue
import org.amshove.kluent.shouldEqual
import org.amshove.kluent.shouldNotBe
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import toothpick.Toothpick
import toothpick.configuration.Configuration
import toothpick.configuration.ConfigurationHolder

class KTPTest {

    @AfterEach
    fun tearDown() {
        Toothpick.reset();
    }

    @Test
    fun `openScope should open scope and wrap it using KTPScope`() {
        // GIVEN
        Toothpick.isScopeOpen("name").shouldBeFalse()

        // WHEN
        val scope = KTP.openScope("name")

        // THEN
        scope shouldBeInstanceOf KTPScope::class
        scope.name shouldEqual "name"
        Toothpick.isScopeOpen("name").shouldBeTrue()
    }

    @Test
    fun `openScopes should open scopes and wrap child scope using KTPScope`() {
        // GIVEN
        Toothpick.isScopeOpen("parent").shouldBeFalse()
        Toothpick.isScopeOpen("child").shouldBeFalse()

        // WHEN
        val scope = KTP.openScopes("parent", "child")

        // THEN
        scope shouldBeInstanceOf KTPScope::class
        scope.name shouldEqual "child"
        Toothpick.isScopeOpen("parent").shouldBeTrue()
        Toothpick.isScopeOpen("child").shouldBeTrue()
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