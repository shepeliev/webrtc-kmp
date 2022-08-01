enableFeaturePreview("VERSION_CATALOGS")

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

includeBuild("build-plugins")

include(":webrtc-kmp")
include(":sample:shared")
include(":sample:app-android")
include(":sample:app-web")
