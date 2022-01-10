@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    kotlin("multiplatform")
    id("org.jetbrains.compose") version libs.versions.jetbrainsCompose
    id("org.jetbrains.kotlin.plugin.serialization") version libs.versions.kotlin
}

kotlin {
    jvm()
    js(IR) {
        browser()
    }
    sourceSets {
        val commonMain by getting {
            dependencies {

                val ktorVersion = "1.6.7"

                implementation(project(":swr"))
                implementation("dev.burnoo:cokoin:0.3.3")
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.3.2")
                implementation("io.ktor:ktor-client-core:$ktorVersion")
                implementation("io.ktor:ktor-client-serialization:$ktorVersion")
            }
        }
    }
}