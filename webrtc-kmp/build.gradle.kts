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
    androidMainApi(fileTree("../vendor/android") { include("*.jar") })
    androidTestImplementation(deps.androidx.test.core)
    androidTestImplementation(deps.androidx.test.runner)
}
