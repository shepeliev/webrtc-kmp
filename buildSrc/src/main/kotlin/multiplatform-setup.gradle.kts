plugins {
    id("com.android.library")
    kotlin("multiplatform")
    id("org.jlleitschuh.gradle.ktlint")
    id("de.undercouch.download")
}

kotlin {
    android()

    js {
        useCommonJs()
        browser()
    }

    jvm()

    sourceSets {
        getByName("commonTest") {
            dependencies {
                implementation(kotlin("test"))
                implementation(kotlin("test-annotations-common"))
            }
        }
    }
}

@Suppress("UnstableApiUsage")
android {
    compileSdk = AndroidConfig.compileSdkVersion

    sourceSets.named("main") {
        manifest.srcFile("src/androidMain/AndroidManifest.xml")
        res.srcDir("src/androidMain/res")
    }

    defaultConfig {
        minSdk = AndroidConfig.minSdkVersion
        targetSdk = AndroidConfig.targetSdkVersion
    }
}
