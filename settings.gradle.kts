pluginManagement {
    repositories {
        google()
        jcenter()
        gradlePluginPortal()
        mavenCentral()
    }
    resolutionStrategy {
        eachPlugin {
            if (requested.id.namespace == "com.android") {
                useModule("com.android.tools.build:gradle:4.1.1")
            }
        }
    }
}
rootProject.name = "webrtc-kmp"
enableFeaturePreview("GRADLE_METADATA")

