plugins {
    id("com.android.application")
    id("kotlin-android")
    id("com.google.gms.google-services")
}

android {
    compileSdk = AndroidConfig.compileSdkVersion

    defaultConfig {
        minSdk = AndroidConfig.minSdkVersion
        targetSdk = AndroidConfig.targetSdkVersion
        versionCode = 1
        versionName = "1.0.0"
        applicationId = "com.shepeliev.webrtckmp.sample"
    }

    buildFeatures {
        compose = true
    }

    compileOptions {
        isCoreLibraryDesugaringEnabled = true
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    kotlinOptions {
        jvmTarget = "1.8"
    }

    composeOptions {
        kotlinCompilerExtensionVersion = deps.versions.androidxCompose.get()
    }
}

dependencies {
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:1.1.5")
    implementation(project(":sample:shared"))
    implementation(deps.androidx.coreKtx)
    implementation(deps.androidx.appcompat)
    implementation(deps.androidx.activity.activityKtx)
    implementation(deps.androidx.compose.activity)
    implementation(deps.androidx.compose.material)
    implementation(deps.androidx.compose.animation)
    implementation(deps.androidx.lifecycle.lifecycleCommonJava8)
    implementation(deps.decompose.compose)
    implementation(deps.accompanist.permissions)
}
