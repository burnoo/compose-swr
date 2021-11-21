import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask

@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    alias(libs.plugins.dependencyUpdates)
}

buildscript {
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        classpath(libs.gradle.android)
        classpath(libs.gradle.kotlin)
    }
}

allprojects {
    repositories {
        google()
        mavenCentral()
    }
}

tasks.withType<DependencyUpdatesTask> {
    rejectVersionIf {
        getStabilityLevel(candidate.version) < getStabilityLevel(currentVersion)
    }
}

fun getStabilityLevel(version: String) : Int {
    val v = version.toUpperCase()
    return when {
        v.contains("ALPHA") -> 0
        v.contains("BETA") -> 1
        v.contains(Regex("(?:RC)|(?:-M[0-9]+)")) -> 2
        else -> 3
    }
}