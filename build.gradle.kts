import com.diffplug.gradle.spotless.SpotlessExtension

plugins {
    id("org.jetbrains.kotlinx.kover") version "0.5.0"
    id("com.diffplug.gradle.spotless") version "6.2.0" apply false
    id("org.jetbrains.kotlin.jvm") version "1.7.0" apply false
}

subprojects {
    pluginManager.apply("com.diffplug.spotless")

    configure<SpotlessExtension> {
        kotlin {
            ktlint()
            licenseHeaderFile(rootProject.file("spotless.license.java.txt"))
        }

        kotlinGradle {
            ktlint()
            target("*.gradle.kts")
        }
    }

    afterEvaluate {
        tasks.withType<Test> {
            testLogging {
                events("passed", "skipped", "failed")
            }
        }
    }

    pluginManager.withPlugin("maven-publish") {
        apply<JavaPlugin>()

        version = extra["VERSION_NAME"] as String
        group = extra["GROUP"] as String

        configure<JavaPluginExtension> {
            withSourcesJar()
            withJavadocJar()
        }

        configure<PublishingExtension> {
            publications {
                create<MavenPublication>("maven") {
                    from(components["java"])

                    groupId = extra["GROUP"] as String
                    artifactId = extra["POM_ARTIFACT_ID"] as String
                    version = extra["VERSION_NAME"] as String

                    pom {
                        packaging = extra["POM_PACKAGING"] as String

                        name.set(extra["POM_NAME"] as String)
                        description.set(extra["POM_DESCRIPTION"] as String)
                        url.set(extra["POM_URL"] as String)

                        scm {
                            url.set(extra["POM_SCM_URL"] as String)
                            connection.set(extra["POM_SCM_CONNECTION"] as String)
                            developerConnection.set(extra["POM_SCM_DEV_CONNECTION"] as String)
                        }

                        licenses {
                            license {
                                name.set(extra["POM_LICENCE_NAME"] as String)
                                url.set(extra["POM_LICENCE_URL"] as String)
                                distribution.set(extra["POM_LICENCE_DIST"] as String)
                            }
                        }

                        developers {
                            developer {
                                id.set(extra["POM_DEVELOPER_ID"] as String)
                                name.set(extra["POM_DEVELOPER_NAME"] as String)
                            }
                        }
                    }
                }
            }
        }
    }
}
