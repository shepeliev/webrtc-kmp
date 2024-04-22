import org.jetbrains.compose.desktop.application.dsl.TargetFormat
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

    cocoapods {
        version = "1.0"
        summary = "Compose app"
        homepage = "not published"
        ios.deploymentTarget = "13.0"

        pod("WebRTC-SDK") {
            version = libs.versions.webrtc.ios.sdk.get()
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

    js {
        browser {
            binaries.executable()
        }
    }

    jvm {
        compilations.all {
            kotlinOptions.jvmTarget = "17"
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
            implementation(libs.kotlin.coroutines)
            implementation(libs.kermit)
            implementation(project(":webrtc-kmp"))
        }

        androidMain.dependencies {
            implementation(libs.kotlin.coroutines.android)
            implementation(libs.compose.ui.tooling.preview)
            implementation(libs.androidx.activity.compose)
            implementation(libs.accompanist.permissions)
        }

        jsMain.dependencies {
            implementation(project.dependencies.platform(libs.kotlin.wrappers.bom))
            implementation(libs.kotlin.wrappers.mui)
            implementation(libs.kotlin.wrappers.react)
            implementation(libs.kotlin.wrappers.reactDom)
            implementation(libs.kotlin.wrappers.emotion)
        }


        jvmMain.dependencies {
            implementation(compose.material)
            implementation(compose.desktop.currentOs)
            implementation(libs.kotlin.coroutines.swing)

            val osName = System.getProperty("os.name").lowercase()
            val hostOS = if (osName.contains("mac")) {
                "macos"
            } else if (osName.contains("linux")) {
                "linux"
            } else if (osName.contains("windows")) {
                "windows"
            } else {
                throw IllegalStateException("Unsupported OS: $osName")
            }
            val hostArch = when (val arch = System.getProperty("os.arch").lowercase()) {
                "amd64" -> "x86_64"
                else -> arch
            }

            implementation(dependencies.variantOf(libs.webrtc.jvm.sdk) { classifier("$hostOS-$hostArch") })
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

compose.desktop {
    application {
        mainClass = "com.shepeliev.webrtckmp.MainKt"
        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "KMPTemplate"
            packageVersion = "1.0.0"
            macOS {
                infoPlist {
                    extraKeysRawXml = macExtraPlistKeys
                }
            }
        }
    }
}

val macExtraPlistKeys = """
    <key>NSCameraUsageDescription</key>
    <string>Camera is required for video calls</string>
    <key>NSMicrophoneUsageDescription</key>
    <string>Microphone is required for audio calls</string>
    <key>NSCameraUseContinuityCameraDeviceType</key>
    <true/>
""".trimIndent()

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
