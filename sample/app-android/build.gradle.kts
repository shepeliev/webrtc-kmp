plugins {
    id("com.android.application")
    id("kotlin-android")
}

android {
    compileSdk = AndroidConfig.compileSdkVersion

    defaultConfig {
        minSdk = AndroidConfig.minSdkVersion
        targetSdk = AndroidConfig.targetSdkVersion
        versionCode = 1
        versionName = "1.0.0"
    }

    buildFeatures {
        compose = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    kotlinOptions {
        jvmTarget = "1.8"
    }

    composeOptions {
        kotlinCompilerExtensionVersion = deps.versions.androidCompose.get()
    }
}

dependencies {
    implementation(deps.android.core)
    implementation(deps.android.appcompat)
    implementation(deps.android.compose.activity)
    implementation(deps.android.compose.material)
    implementation(deps.android.compose.animation)
}
