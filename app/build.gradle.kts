plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "ru.axas.roulette"
    compileSdk = 33

    defaultConfig {
        applicationId = "ru.axas.roulette"
        minSdk = 24
        targetSdk = 33
        versionCode = 1
        versionName  = "1.0($versionCode)"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables.useSupportLibrary = true
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
        debug {
            versionNameSuffix = "-DEBUG"
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions { jvmTarget = "17" }

    buildFeatures {
        buildConfig = true
        compose = true
    }

    composeOptions { kotlinCompilerExtensionVersion = "1.4.5" }
}

dependencies {

    val composeUiVersion = "1.4.3"

    implementation ("androidx.core:core-ktx:1.10.1")

    implementation ("androidx.compose.ui:ui:$composeUiVersion")
    implementation ("androidx.compose.material3:material3:1.2.0-alpha01")
    implementation ("androidx.compose.ui:ui-tooling-preview:$composeUiVersion")

    implementation ("androidx.lifecycle:lifecycle-runtime-ktx:2.6.1")
    implementation ("androidx.activity:activity-compose:1.7.1")
}