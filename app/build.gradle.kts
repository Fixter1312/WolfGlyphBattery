plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.example.wolfglyphbattery"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.wolfglyphbattery"
        minSdk = 34
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        // żeby nie sypało błędem o launcherze – użyjemy systemowej ikony
        resourceConfigurations += listOf("en", "pl")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
            isMinifyEnabled = false
        }
    }

    buildFeatures {
        viewBinding = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")

    // Twój AAR (lokalny)
    implementation(files("libs/glyph-matrix-sdk-1.0.aar"))
}
