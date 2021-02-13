pluginManagement {
    repositories {
        google()
        jcenter()
        gradlePluginPortal()
        mavenCentral()
    }

}
rootProject.name = "AppRtcKmm"
enableFeaturePreview("GRADLE_METADATA")

include(":androidAppKt")
include(":androidApp")
include(":shared")
include(":webRtcKmm")

