import de.undercouch.gradle.tasks.download.Download

plugins {
    id("multiplatform-setup")
    id("publish-setup")
}

group = "com.shepeliev"
version = "0.106.0"

val jitsiWebRtcVersion = "106.0.1"

kotlin {
    android {
        publishAllLibraryVariants()
    }

    ios {
        val frameworks = getFrameworks(konanTarget).filterKeys { it == "WebRTC" }

        compilations.getByName("main") {
            cinterops.create("WebRTC") {
                frameworks.forEach { (framework, path) ->
                    compilerOpts("-framework", framework, "-F$path")
                }
            }
        }

        binaries {
            getTest("DEBUG").apply {
                frameworks.forEach { (framework, path) ->
                    linkerOpts("-framework", framework, "-F$path", "-rpath", "$path", "-ObjC")
                }
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
    jsMainImplementation(npm("webrtc-adapter", "8.1.1"))
}

tasks.register<Download>("downloadAndroidWebRtc") {
    src("https://github.com/jitsi/webrtc/releases/download/v$jitsiWebRtcVersion/android-webrtc.zip")
    dest(buildDir.resolve("tmp/android-webrtc-$jitsiWebRtcVersion.zip"))
    overwrite(false)
}

tasks.register<Copy>("unzipAndroidWebRtc") {
    inputs.files(tasks.findByName("downloadAndroidWebRtc")!!.outputs.files)
    from(zipTree(inputs.files.first()))
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
