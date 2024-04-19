import org.jetbrains.kotlin.gradle.plugin.mpp.NativeBuildType

plugins {
    id("multiplatform-setup")
    kotlin("native.cocoapods")
}

kotlin {
    cocoapods {
        version = "1.0.0"
        summary = "Shared framework for WebRTC KMP sample"
        homepage = "https://github.com/shepeliev/webrtc-kmp/tree/main/sample"
        ios.deploymentTarget = "11.0"

        pod("FirebaseCore")
        pod("FirebaseFirestore")
        pod("WebRTC-SDK") {
            version = "114.5735.02"
            linkOnly = true
        }

        podfile = project.file("../app-ios/Podfile")

        framework {
            baseName = "shared"
            export(project(":webrtc-kmp"))
            export(deps.decompose)
            transitiveExport = true
        }

        xcodeConfigurationToNativeBuildType["CUSTOM_DEBUG"] = NativeBuildType.DEBUG
        xcodeConfigurationToNativeBuildType["CUSTOM_RELEASE"] = NativeBuildType.RELEASE
    }

    androidTarget()

    iosX64()
    iosArm64()
    iosSimulatorArm64()

    js {
        useCommonJs()
        browser()
    }

    sourceSets {
        commonMain.dependencies {
            api(project(":webrtc-kmp"))
            api(deps.decompose)
            implementation(deps.kotlin.coroutines)
            implementation(deps.kermit)
        }

        androidMain.dependencies {
            implementation(project.dependencies.platform(deps.firebase.bom))
            implementation(deps.firebase.firestore)
            implementation(deps.kotlin.coroutinesPlayServices)
        }

        jsMain.dependencies {
            implementation(npm("firebase", version = "9.9.1"))
        }
    }
}

android {
    namespace = "com.shepeliev.webrtckmp.sample.shared"
}
