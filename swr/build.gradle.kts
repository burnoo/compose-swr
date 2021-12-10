@file:Suppress("EXPERIMENTAL_IS_NOT_ENABLED")
import org.jetbrains.compose.ExperimentalComposeLibrary

@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    kotlin("multiplatform")
    id("org.jetbrains.compose") version libs.versions.jetbrainsCompose
}

kotlin {
    jvm()
    js(IR) {
        browser()
    }
    sourceSets {
        named("commonMain") {
            dependencies {
                api(compose.runtime)
                implementation(libs.coroutines)
                implementation(libs.datetime)
            }
        }
        named("jvmTest") {
            dependencies {
                @OptIn(ExperimentalComposeLibrary::class)
                implementation(compose.uiTestJUnit4)
                implementation(getSkiaDependency())
                implementation(compose.material)
                implementation(libs.coroutines.test)
                implementation(libs.testCore)
            }
        }
    }

    sourceSets.all {
        languageSettings.optIn("kotlin.RequiresOptIn")
    }
}

fun getSkiaDependency() : String {
    val osName = System.getProperty("os.name")
    val targetOs = when {
        osName == "Mac OS X" -> "macos"
        osName.startsWith("Win") -> "windows"
        osName.startsWith("Linux") -> "linux"
        else -> error("Unsupported OS: $osName")
    }

    val targetArch = when (val osArch = System.getProperty("os.arch")) {
        "x86_64", "amd64" -> "x64"
        "aarch64" -> "arm64"
        else -> error("Unsupported arch: $osArch")
    }

    val version = "0.6.7"
    val target = "${targetOs}-${targetArch}"
    return "org.jetbrains.skiko:skiko-jvm-runtime-$target:$version"
}