import org.jetbrains.kotlin.gradle.targets.js.dsl.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpackConfig
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget


plugins {
    kotlin("multiplatform")
    id("com.android.application")
    kotlin("native.cocoapods")
    alias(libs.plugins.jetbrains.compose)
}

kotlin {
    configureKotlinCompilerArgs()

//    @OptIn(ExperimentalWasmDsl::class)
//    wasmJs {
//        moduleName = "composeApp"
//        browser {
//            commonWebpackConfig {
//                outputFileName = "composeApp.js"
//                devServer = (devServer ?: KotlinWebpackConfig.DevServer()).apply {
//                    static = (static ?: mutableListOf()).apply {
//                        // Serve sources to debug inside browser
//                        add(project.projectDir.path)
//                    }
//                }
//            }
//        }
//        binaries.executable()
//    }

    cocoapods {
        version = "1.0"
        summary = "Compose app"
        homepage = "not published"
        ios.deploymentTarget = "13.0"

        pod("WebRTC-SDK") {
            version = "114.5735.02"
            moduleName = "WebRTC"
            packageName = "WebRTC"
        }
    }

    androidTarget {
        configureJvmTarget()
    }

    listOf(
        iosX64 { configureWebRtcCinterops() },
        iosArm64 { configureWebRtcCinterops() },
        iosSimulatorArm64 { configureWebRtcCinterops() }
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "ComposeApp"
            isStatic = true
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
            implementation(libs.kermit)
            implementation(project(":webrtc-kmp"))
        }

        androidMain.dependencies {
            implementation(libs.compose.ui.tooling.preview)
            implementation(libs.androidx.activity.compose)
            implementation(libs.accompanist.permissions)
        }
    }
}

android {
    namespace = "com.shepeliev.webrtckmp.sample"
    compileSdk = libs.versions.compileSdk.get().toInt()

    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
    sourceSets["main"].res.srcDirs("src/androidMain/res")
    sourceSets["main"].resources.srcDirs("src/commonMain/resources")

    defaultConfig {
        applicationId = "com.shepeliev.webrtckmp.sample"
        minSdk = libs.versions.minSdk.get().toInt()
        targetSdk = libs.versions.targetSdk.get().toInt()
        versionCode = 1
        versionName = "1.0"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    dependencies {
        debugImplementation(libs.compose.ui.tooling)
    }
}

compose.experimental {
    web.application {}
}

fun KotlinNativeTarget.configureWebRtcCinterops() {
    val webRtcFrameworkPath = file("$buildDir/cocoapods/synthetic/IOS/Pods/WebRTC-SDK")
        .resolveArchPath(konanTarget, "WebRTC")
    compilations.getByName("main") {
        cinterops.getByName("WebRTC") {
            compilerOpts("-framework", "WebRTC", "-F$webRtcFrameworkPath")
        }
    }

    binaries {
        getTest("DEBUG").apply {
            linkerOpts(
                "-framework",
                "WebRTC",
                "-F$webRtcFrameworkPath",
                "-rpath",
                "$webRtcFrameworkPath",
                "-ObjC"
            )
        }
    }
}
