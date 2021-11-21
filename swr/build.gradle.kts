plugins {
    kotlin("multiplatform")
    id("org.jetbrains.compose") version "1.0.0-beta5"
}

kotlin {
    jvm()
    sourceSets {
        named("commonMain") {
            dependencies {
                api(compose.runtime)
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.5.2")
                implementation("org.jetbrains.kotlinx:kotlinx-datetime:${rootProject.extra["datetime_version"]}")
            }
        }
        named("jvmTest") {
            dependencies {
                implementation(compose.uiTestJUnit4)
                implementation(getSkiaDependency())
                implementation(compose.material)
                implementation("junit:junit:4.13.2")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.5.2")
                implementation("androidx.test:core:1.4.0")
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

    val version = "0.5.9"
    val target = "${targetOs}-${targetArch}"
    return "org.jetbrains.skiko:skiko-jvm-runtime-$target:$version"
}