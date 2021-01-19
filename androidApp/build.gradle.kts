plugins {
    id("com.android.application")
    kotlin("android")
}

dependencies {
    implementation(fileTree("libs"))
    implementation(project(":shared"))
    implementation("com.google.android.material:material:1.2.1")
    implementation("androidx.appcompat:appcompat:1.2.0")
    implementation("androidx.constraintlayout:constraintlayout:2.0.2")
    implementation("org.webrtc:google-webrtc:1.0.32006")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.4.2")
}

android {
    compileSdkVersion(30)
    defaultConfig {
        applicationId = "com.shepeliev.apprtckmm"
        minSdkVersion(24)
        targetSdkVersion(30)
        versionCode = 1
        versionName = "1.0"
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }

    kotlinOptions {
        jvmTarget = "1.8"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}
