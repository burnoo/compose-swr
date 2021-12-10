import org.jetbrains.compose.desktop.application.dsl.TargetFormat

@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    kotlin("jvm")
    id("org.jetbrains.compose") version libs.versions.jetbrainsCompose
}

compose.desktop {
    application {
        mainClass = "dev.burnoo.compose.swr.example.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)

            packageVersion = "1.0.0"

            macOS {

            }
            windows {
                menuGroup = "Compose Examples"
                // see https://wixtoolset.org/documentation/manual/v3/howtos/general/generate_guids.html
                upgradeUuid = "18159995-d967-4CD2-8885-77CFA97CFA9F"
            }
            linux {

            }
        }
    }
}

dependencies {
    implementation(compose.desktop.currentOs)
    implementation(compose.material)
    implementation(project(":example:common"))
    implementation("io.ktor:ktor-client-cio:1.6.7")
    implementation("dev.burnoo:cokoin:0.3.2")
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