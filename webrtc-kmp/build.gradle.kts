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
    namespace = "com.shepeliev.webrtckmp"

    defaultConfig {
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
}

dependencies {
    commonMainImplementation(deps.kotlin.coroutines)
    androidMainImplementation(deps.androidx.coreKtx)
    androidMainApi(deps.webrtcSdk)
    androidTestImplementation(deps.androidx.test.core)
    androidTestImplementation(deps.androidx.test.runner)
    jsMainImplementation(npm("webrtc-adapter", "8.1.1"))
}
