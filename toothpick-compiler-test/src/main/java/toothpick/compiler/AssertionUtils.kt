package toothpick.compiler

import com.google.devtools.ksp.processing.SymbolProcessorProvider
import com.tschuchort.compiletesting.*
import org.junit.Assert.*
import java.io.File

data class AssertInitial(
    val sources: List<SourceFile> = emptyList(),
    val symbolProcessorProviders: List<SymbolProcessorProvider> = emptyList(),
    val kspOptions: Map<String, String> = emptyMap(),
    val logVerbose: Boolean = false
)

data class AssertCompilable(
    val initial: AssertInitial,
    val compilation: KotlinCompilation
)

data class AssertCompiled(
    val compilable: AssertCompilable,
    val result: KotlinCompilation.Result
)

fun compilationAssert(): AssertInitial = AssertInitial()

fun AssertInitial.that(vararg sources: SourceFile): AssertInitial =
    copy(sources = sources.toList())

fun AssertInitial.processedWith(vararg symbolProcessorProviders: SymbolProcessorProvider): AssertInitial =
    copy(symbolProcessorProviders = symbolProcessorProviders.toList())

fun AssertInitial.withOptions(vararg kspOptions: Pair<String, String>): AssertInitial =
    copy(kspOptions = kspOptions.toMap())

fun AssertInitial.logVerbose(): AssertInitial =
    copy(logVerbose = true)

private fun AssertInitial.asCompilation(): AssertCompilable {
    val builder = this
    return AssertCompilable(
        initial = this,
        compilation = KotlinCompilation().apply {
            inheritClassPath = true
            verbose = logVerbose
            sources = builder.sources
            symbolProcessorProviders = builder.symbolProcessorProviders
            kspArgs = kspOptions.toMutableMap()
        }
    )
}

private fun AssertCompilable.compile(): AssertCompiled {
    return AssertCompiled(
        compilable = this,
        result = compilation.compile()
    )
}

fun AssertInitial.compilesWithoutError(): AssertCompiled =
    asCompilation()
        .compile()
        .apply {
            assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode)
        }

fun AssertInitial.failsToCompile(): AssertCompiled =
    asCompilation()
        .compile()
        .apply {
            assertEquals(KotlinCompilation.ExitCode.COMPILATION_ERROR, result.exitCode)
        }

fun AssertCompiled.assertLogs(message: String) = apply {
    assertTrue(result.messages.contains(message))
}

fun AssertCompiled.generatesSources(vararg expected: RawSource) = apply {
    expected.forEach { source -> generatesSource(source) }
}

private fun AssertCompiled.generatesSource(expected: RawSource) = apply {
    val outputDir = compilable.compilation.kspSourcesDir.resolve("kotlin")
    val actual: File = outputDir.resolve(expected.fileName)

    if (!actual.exists()) {
        fail(
            "Expected file %s does not exist on disk. Actual files: %s".format(
                actual.toRelativeString(outputDir),
                outputDir.walk()
                    .filter { it.isFile }
                    .map { it.name }
                    .toList()
            )
        )
    }

    assertEquals(
        expected.contents.trim(),
        actual.readText()
            .replace("\r\n", "\n")
            .trim()
    )
}

fun AssertCompiled.generatesFileNamed(relativeName: String) = apply {
    val outputDir = compilable.compilation.kspSourcesDir.resolve("kotlin")
    val expected = outputDir.resolve(relativeName)

    if (!expected.exists()) {
        fail(
            "Expected file %s does not exist on disk. Actual files: %s".format(
                expected.toRelativeString(outputDir),
                outputDir.walk()
                    .filter { it.isFile }
                    .map { it.name }
                    .toList()
            )
        )
    }
}
