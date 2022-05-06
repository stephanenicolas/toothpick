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
    implementation(libs.ksp)
    implementation(projects.toothpickCompilerKspFactory)
    implementation(projects.toothpickCompilerKspMemberinjector)

    testImplementation(libs.junit4)
    testImplementation(libs.compiletesting.kt)
    testImplementation(projects.toothpickCompilerTest)
}
