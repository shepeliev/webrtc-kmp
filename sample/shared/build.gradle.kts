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

    ios()
    iosSimulatorArm64()
    jvm()

    sourceSets {
        val iosMain by getting
        val iosSimulatorArm64Main by getting
        iosSimulatorArm64Main.dependsOn(iosMain)

        val iosTest by getting
        val iosSimulatorArm64Test by getting
        iosSimulatorArm64Test.dependsOn(iosTest)
    }
}

android {
    namespace = "com.shepeliev.webrtckmp.sample.shared"
}

dependencies {
    commonMainApi(project(":webrtc-kmp"))
    commonMainApi(deps.decompose)
    commonMainImplementation(deps.kotlin.coroutines)
    commonMainImplementation(deps.kermit)
    androidMainImplementation(platform(deps.firebase.bom))
    androidMainImplementation(deps.firebase.firestore)
    androidMainImplementation(deps.kotlin.coroutinesPlayServices)
    jsMainImplementation(npm("firebase", version = "9.9.1"))
    jvmMainImplementation(deps.firebase.firestore.jvm)

    jvmMainImplementation(
        group = "dev.onvoid.webrtc",
        name = "webrtc-java",
        version = "0.8.0",
        classifier = "windows-x86_64"
    )
    jvmMainImplementation(
        group = "dev.onvoid.webrtc",
        name = "webrtc-java",
        version = "0.8.0",
        classifier = "macos-aarch64"
    )
    jvmMainImplementation(
        group = "dev.onvoid.webrtc",
        name = "webrtc-java",
        version = "0.8.0",
        classifier = "linux-x86_64"
    )
}
