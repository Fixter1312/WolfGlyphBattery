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
        resourceConfigurations += listOf("en", "pl")
    }

    buildTypes {
        debug {
            // minify OFF, ale podłączamy rules – to ważne dla reflection
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        release {
            // możesz włączyć minify w release, jak chcesz
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
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

repositories {
    // NIE jest wymagane przy implementation(files(...)),
    // ale zostawiamy – nie przeszkadza.
    flatDir { dirs("libs") }
}

dependencies {
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")

    // AAR lokalnie z folderu app/libs/
    implementation(files("libs/glyph-matrix-sdk-1.0.aar"))
}
