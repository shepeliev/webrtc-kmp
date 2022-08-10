import de.undercouch.gradle.tasks.download.Download

plugins {
    id("multiplatform-setup")
    id("publish-setup")
}

group = "com.shepeliev"
version = "0.89.7"

val jitsiWebRtcVersion = "100.0.2"

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
    src("https://github.com/jitsi/webrtc/releases/download/v$jitsiWebRtcVersion/android-webrtc.tgz")
    dest(buildDir.resolve("tmp/android-webrtc-$jitsiWebRtcVersion.tgz"))
    overwrite(false)
}

tasks.register<Copy>("unzipAndroidWebRtc") {
    from(tarTree(resources.gzip(tasks.findByName("downloadAndroidWebRtc")!!.outputs.files.first())))
    into(buildDir.resolve("libs/android"))
}

afterEvaluate {
    tasks.named("preBuild") {
        dependsOn("unzipAndroidWebRtc")
    }

    // downloads Android WebRTC lib on gradle sync in Android Studio
    tasks.named("commonize") {
        dependsOn("unzipAndroidWebRtc")
    }
}
