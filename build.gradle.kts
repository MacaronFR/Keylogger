import java.io.ByteArrayOutputStream

plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm") version "1.9.0"
    id("org.jetbrains.intellij") version "1.15.0"
}

group = "fr.imacaron"
version = "1.1"

repositories {
    mavenCentral()
}

// Configure Gradle IntelliJ Plugin
// Read more: https://plugins.jetbrains.com/docs/intellij/tools-gradle-intellij-plugin.html
intellij {
    version.set("2023.2")
    type.set("IC") // Target IDE Platform

    plugins.set(listOf(/* Plugin Dependencies */))
}

tasks {
    // Set the JVM compatibility versions
    withType<JavaCompile> {
        sourceCompatibility = "17"
        targetCompatibility = "17"
    }
    withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions.jvmTarget = "17"
    }

    patchPluginXml {
        sinceBuild.set("222")
        untilBuild.set("232.*")
    }

    signPlugin {
        certificateChain.set(System.getenv("CERTIFICATE_CHAIN"))
        privateKey.set(System.getenv("PRIVATE_KEY"))
        password.set(System.getenv("PRIVATE_KEY_PASSWORD"))
    }

    publishPlugin {
        val out = ByteArrayOutputStream()

        exec {
            commandLine = listOf("pass", "show", "jetbrains-plugin-token")
            standardOutput = out
        }

        token.set(out.toString().removeSuffix("\n"))
        channels.set(listOf("Stable"))
    }

    signPlugin {
        val out = ByteArrayOutputStream()

        exec {
            commandLine = listOf("pass", "show", "jetbrains-private-key-password")
            standardOutput = out
        }

        privateKey.set(file("./private.pem").readText())
        password.set(out.toString().removeSuffix("\n"))
        certificateChain.set(file("./chain.crt").readText())
    }
}
