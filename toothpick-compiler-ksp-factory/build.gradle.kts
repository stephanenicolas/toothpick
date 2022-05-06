import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("org.jetbrains.kotlin.jvm")
    `maven-publish`
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {
    api(libs.tp)
    api(libs.inject)

    implementation(libs.ksp)
    implementation(libs.kotlinpoet.core)
    implementation(libs.kotlinpoet.ksp)
    implementation(projects.toothpickCompilerKspCore)

    testImplementation(libs.junit4)
    testImplementation(libs.compiletesting.kt)
    testImplementation(projects.toothpickCompilerTest)
}
