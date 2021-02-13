import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget

plugins {
    kotlin("multiplatform")
    kotlin("native.cocoapods")
    id("com.android.library")
}

version = "1.0.0"

kotlin {
    cocoapods {
        summary = "WebRTC Kotlin multi platform SDK"
        homepage = "https://github.com/shepeliev/webrtc-kmp"
        ios.deploymentTarget = "9.0"

        specRepos {
            url("https://github.com/CocoaPods/Specs.git")
        }

        pod("GoogleWebRTC") {
            version = "~> 1.1"
            moduleName = "WebRTC"
        }
    }

    android()

    val iosTarget: (String, KotlinNativeTarget.() -> Unit) -> KotlinNativeTarget =
        if (System.getenv("SDK_NAME")?.startsWith("iphoneos") == true)
            ::iosArm64
        else
            ::iosX64

    iosTarget("ios") {
        binaries {
            getTest("DEBUG").apply {
                linkerOpts(
                    "-framework",
                    "WebRTC",
                    "-F${rootProject.projectDir}/webRtcKmm/build/cocoapods/synthetic/IOS/webRtcKmm/Pods/GoogleWebRTC/Frameworks/frameworks",
                    "-rpath",
                    "${rootProject.projectDir}/webRtcKmm/build/cocoapods/synthetic/IOS/webRtcKmm/Pods/GoogleWebRTC/Frameworks/frameworks"
                )
            }
        }
    }

    sourceSets {
        val commonMain by getting
        val androidMain by getting {
            dependencies {
                implementation("org.webrtc:google-webrtc:1.0.32006")
            }
        }
        val iosMain by getting

        val commonTest by getting {
            dependencies {
                implementation(kotlin("test-common"))
                implementation(kotlin("test-annotations-common"))
            }
        }

        val iosTest by getting
    }
}

android {
    compileSdkVersion(30)
    defaultConfig {
        minSdkVersion(24)
        targetSdkVersion(30)
    }
    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

val packForXcode by tasks.creating(Sync::class) {
    group = "build"
    val mode = System.getenv("CONFIGURATION") ?: "DEBUG"
    val sdkName = System.getenv("SDK_NAME") ?: "iphonesimulator"
//    val targetName = "ios" + if (sdkName.startsWith("iphoneos")) "Arm64" else "X64"
    val targetName = "ios"
    val framework =
        kotlin.targets.getByName<KotlinNativeTarget>(targetName).binaries.getFramework(mode)
    inputs.property("mode", mode)
    dependsOn(framework.linkTask)
    val targetDir = File(buildDir, "xcode-frameworks")
    from({ framework.outputDirectory })
    into(targetDir)
}
tasks.getByName("build").dependsOn(packForXcode)
