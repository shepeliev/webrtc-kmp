@Suppress("UnstableApiUsage")
dependencyResolutionManagement {
    repositories {
        mavenLocal()
        mavenCentral()
        google()
    }
}

pluginManagement {
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
    }
}

rootProject.name = "webrtc-kmp"
include(":webrtc-kmp")
 include(":sample:composeApp")
// include(":sample:app-android")
// include(":sample:app-web")
