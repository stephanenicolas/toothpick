package toothpick.compiler

import com.tschuchort.compiletesting.SourceFile
import org.intellij.lang.annotations.Language

data class RawSource(
    val fileName: String,
    val contents: String
)

fun javaSource(name: String, @Language("java") contents: String): SourceFile =
    SourceFile.java("$name.java", contents, trimIndent = true)

fun ktSource(name: String, @Language("kotlin") contents: String): SourceFile =
    SourceFile.java("$name.kt", contents, trimIndent = true)

fun expectedJavaSource(name: String, @Language("java") contents: String): RawSource =
    RawSource("$name.java", contents.trimIndent())

fun expectedKtSource(name: String, @Language("kotlin") contents: String): RawSource =
    RawSource("$name.kt", contents.trimIndent())
