@Suppress("UnstableApiUsage")
dependencyResolutionManagement {
    versionCatalogs {
        create("deps") {
            from(files("libs.versions.toml"))
        }
    }

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

rootProject.name = "WebRTC"

include(":webrtc-kmp")
include(":sample:shared")
include(":sample:app-android")
include(":sample:app-web")
include(":sample:app-jvm")
