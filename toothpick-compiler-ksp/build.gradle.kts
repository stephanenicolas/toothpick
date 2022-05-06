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
    implementation(project(":toothpick-compiler-ksp-factory"))
    implementation(project(":toothpick-compiler-ksp-memberinjector"))

    implementation(libs.ksp)

    testImplementation(project(":toothpick-compiler-test"))
    testImplementation(libs.junit4)
    testImplementation(libs.compiletesting.kt)
}
