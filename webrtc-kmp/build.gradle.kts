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
                compilerOpts("-framework", "WebRTC", "-F${resolveFrameworkPath("WebRTC")}")
            }
        }
    }
}

dependencies {
    commonMainImplementation(deps.kotlin.coroutines)
    androidMainImplementation(deps.androidx.coreKtx)
    androidMainApi(fileTree("../vendor/android") { include("*.jar") })
}
