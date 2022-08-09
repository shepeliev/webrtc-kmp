import de.undercouch.gradle.tasks.download.Download

plugins {
    id("multiplatform-setup")
    id("publish-setup")
}

group = "com.shepeliev"
version = "0.89.7"

kotlin {
    android {
        publishAllLibraryVariants()
    }

    ios {
        compilations.getByName("main") {
            cinterops.create("WebRTC") {
                compilerOpts("-framework", "WebRTC", "-F${resolveFrameworkPath("WebRTC", ::webrtcArchVariant)}")
            }
        }

        binaries {
            getTest("DEBUG").apply {
                linkerOpts(
                    "-framework",
                    "WebRTC",
                    "-F${resolveFrameworkPath("WebRTC", ::webrtcArchVariant)}",
                    "-rpath",
                    "${resolveFrameworkPath("WebRTC", ::webrtcArchVariant)}",
                    "-ObjC"
                )
            }
        }
    }
}

android {
    defaultConfig {
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
}

dependencies {
    commonMainImplementation(deps.kotlin.coroutines)
    androidMainImplementation(deps.androidx.coreKtx)
    androidMainApi(fileTree("build/libs/android") { include("*.jar") })
    androidTestImplementation(deps.androidx.test.core)
    androidTestImplementation(deps.androidx.test.runner)
}

tasks.register<Download>("downloadAndroidWebRtc") {
    src(listOf(
        "https://github.com/react-native-webrtc/react-native-webrtc/raw/1.89.3/android/libs/libjingle_peerconnection.so.jar",
        "https://github.com/react-native-webrtc/react-native-webrtc/raw/1.89.3/android/libs/libwebrtc.jar",
    ))

    dest(buildDir.resolve("libs/android"))
    overwrite(false)
}

afterEvaluate {
    tasks.named("preBuild") {
        dependsOn("downloadAndroidWebRtc")
    }
}
