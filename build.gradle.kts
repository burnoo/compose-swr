buildscript {
    extra.apply {
        set("lib_version", "0.0.1")
        set("compose_version", "1.0.5")
        set("koin_version", "3.1.2")
        set("ktor_version", "1.6.4")
        set("datetime_version", "0.3.1")
    }

    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        classpath("com.android.tools.build:gradle:4.2.2")
        classpath(kotlin("gradle-plugin", version = "1.5.31"))
    }
}

plugins {
    id("maven-publish")
}

tasks.register<Delete>("clean").configure {
    delete(rootProject.buildDir)
}