plugins {
    id("com.android.application")
    kotlin("android")
}

android {
    compileSdk = 31

    defaultConfig {
        targetSdk = 31
        minSdk = 24

        applicationId = "dev.burnoo.compose.swr.sample"
        versionCode = 1
        versionName = "1.0"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    kotlinOptions {
        jvmTarget = "1.8"
    }

    buildFeatures {
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.1.0-rc02"
    }
}

dependencies {

    val jetpackComposeVersion = "1.0.5"

    implementation(project(":swr"))
    implementation(project(":example:common"))
    implementation("androidx.compose.ui:ui:$jetpackComposeVersion")
    implementation("androidx.compose.material:material:$jetpackComposeVersion")
    implementation("androidx.compose.ui:ui-tooling:$jetpackComposeVersion")
    implementation("androidx.activity:activity-compose:1.4.0")
    implementation("com.google.android.material:material:1.4.0")
    implementation("io.ktor:ktor-client-android:1.6.7")
    implementation("dev.burnoo:cokoin:0.3.3")
}