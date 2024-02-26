plugins {
    id("com.android.application")
    id("kotlin-android")
    id("com.google.gms.google-services")
    id("org.jlleitschuh.gradle.ktlint")
}

android {
    namespace = "com.shepeliev.webrtckmp.sample"

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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.8"
    }
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>() {
    kotlinOptions {
        freeCompilerArgs += listOf(
            "-P",
            "plugin:androidx.compose.compiler.plugins.kotlin:suppressKotlinVersionCompatibilityCheck=true"
        )
    }
}

dependencies {
    implementation(project(":sample:shared"))
    implementation(deps.androidx.coreKtx)
    implementation(deps.androidx.appcompat)
    implementation(deps.androidx.activity.activityKtx)
    implementation(deps.androidx.compose.activity)
    implementation(deps.androidx.compose.material)
    implementation(deps.androidx.compose.animation)
    implementation(deps.androidx.lifecycle.runtime)
    implementation(deps.decompose.compose)
    implementation(deps.accompanist.permissions)
}
