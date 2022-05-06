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
    api(libs.inject)
    api(libs.tp)

    implementation(project(":toothpick-compiler-ksp-core"))

    implementation(libs.ksp)

    implementation(libs.kotlinpoet.core)
    implementation(libs.kotlinpoet.ksp)

    testImplementation(project(":toothpick-compiler-test"))
    testImplementation(libs.junit4)
    testImplementation(libs.compiletesting.kt)
}
