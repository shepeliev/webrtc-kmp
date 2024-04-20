plugins {
    id("com.android.library")
    kotlin("multiplatform")
}

kotlin {
    configureKotlinCompilerArgs()

    androidTarget {
        configureJvmTarget()
    }
}

android {
    compileSdk = androidCompileSdkVersion

    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
    sourceSets["main"].res.srcDir("src/androidMain/res")

    defaultConfig {
        minSdk = androidMinSdkVersion
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

private val Project.versionCatalog: VersionCatalog
    get() = extensions.getByType<VersionCatalogsExtension>().named("libs")

private val Project.androidCompileSdkVersion: Int
    get() = "${versionCatalog.findVersion("compileSdk").get()}".toInt()

private val Project.androidMinSdkVersion: Int
    get() = "${versionCatalog.findVersion("minSdk").get()}".toInt()
