package toothpick.compiler

import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import org.junit.Assert.*
import javax.annotation.processing.Processor

data class Builder(
    val sources: List<SourceFile> = emptyList(),
    val annotationProcessors: List<Processor> = emptyList()
)

fun compilationAssert(): Builder = Builder()

fun Builder.that(vararg sources: SourceFile): Builder =
    copy(sources = sources.toList())

fun Builder.processedWith(annotationProcessors: List<Processor>): Builder =
    copy(annotationProcessors = annotationProcessors)

private fun Builder.compile(configure: KotlinCompilation.() -> Unit = {}): KotlinCompilation.Result {
    val builder = this
    return KotlinCompilation().apply {
        inheritClassPath = true
        verbose = false
        this.sources = builder.sources
        this.annotationProcessors = builder.annotationProcessors
        configure()
    }.compile()
}

fun Builder.compilesWithoutError(): KotlinCompilation.Result = compile().apply {
    assertEquals(KotlinCompilation.ExitCode.OK, exitCode)
}

fun Builder.failsToCompile(): KotlinCompilation.Result = compile().apply {
    assertEquals(KotlinCompilation.ExitCode.COMPILATION_ERROR, exitCode)
}

fun KotlinCompilation.Result.withErrorContaining(error: String): KotlinCompilation.Result = apply {
    assertTrue(messages.contains(error))
}

fun KotlinCompilation.Result.generatesSources(vararg expected: RawSource) = apply {
    expected.forEach { source -> generatesSource(source) }
}

private fun KotlinCompilation.Result.generatesSource(expected: RawSource) = apply {
    val actual = sourcesGeneratedByAnnotationProcessor.find { file -> file.name == expected.fileName }
        ?: error("File ${expected.fileName} not found in: ${sourcesGeneratedByAnnotationProcessor.map { it.name }}")

    assertEquals(
        expected.contents.trim(),
        actual.readText()
            .replace("\r\n", "\n")
            .trim()
    )
}

fun KotlinCompilation.Result.generatesFileNamed(relativeName: String) = apply {
    val generatedFile = generatedFiles.find { file -> file.name == relativeName }
    assertNotNull(generatedFile)
}
