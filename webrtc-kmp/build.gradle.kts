plugins {
    id("multiplatform-setup")
    kotlin("native.cocoapods")
    id("maven-publish")
    id("signing")
}

group = "com.shepeliev"

val webRtcKmpVersion: String by properties
version = webRtcKmpVersion

kotlin {
    cocoapods {
        version = webRtcKmpVersion
        summary = "WebRTC Kotlin Multiplatform SDK"
        homepage = "https://github.com/shepeliev/webrtc-kmp"
        ios.deploymentTarget = "10.0"

        pod("WebRTC-SDK") {
            version = "114.5735.02"
            moduleName = "WebRTC"
            packageName = "WebRTC"
        }
    }

    androidTarget {
        publishAllLibraryVariants()
    }

    iosX64 { configureIos() }
    iosArm64 { configureIos() }
    iosSimulatorArm64 { configureIos() }

    js {
        useCommonJs()
        browser()
    }

    sourceSets {
        commonMain.dependencies {
            implementation(deps.kotlin.coroutines)
        }

        androidMain.dependencies {
            implementation(deps.kotlin.coroutines)
            implementation(deps.androidx.coreKtx)
            api(deps.webrtcSdk)
        }

        jsMain.dependencies {
            implementation(npm("webrtc-adapter", "8.1.1"))
        }

        commonTest.dependencies {
            implementation(kotlin("test"))
            implementation(kotlin("test-annotations-common"))
        }
    }
}

android {
    namespace = "com.shepeliev.webrtckmp"

    defaultConfig {
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    dependencies {
        androidTestImplementation(deps.androidx.test.core)
        androidTestImplementation(deps.androidx.test.runner)
    }
}

publishing {
    publications.all {
        this as MavenPublication

        pom {
            name.set(project.name)
            description.set("WebRTC Kotlin Multiplatform SDK")
            url.set("https://github.com/shepeliev/webrtc-kmp")

            scm {
                url.set("https://github.com/shepeliev/webrtc-kmp")
                connection.set("scm:git:https://github.com/shepeliev/webrtc-kmp.git")
                developerConnection.set("scm:git:https://github.com/shepeliev/webrtc-kmp.git")
                tag.set("HEAD")
            }

            issueManagement {
                system.set("GitHub Issues")
                url.set("https://github.com/shepeliev/webrtc-kmp/issues")
            }

            developers {
                developer {
                    name.set("Alex Shepeliev")
                    email.set("a.shepeliev@gmail.com")
                }
            }

            licenses {
                license {
                    name.set("The Apache Software License, Version 2.0")
                    url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                    distribution.set("repo")
                    comments.set("A business-friendly OSS license")
                }
            }
        }
    }
}

signing {
    val signingKey: String by extra
    val signingPassword: String by extra

    useInMemoryPgpKeys(signingKey, signingPassword)
    sign(publishing.publications)
}

fun org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget.configureIos() {
    val webRtcFrameworkPath = file("$buildDir/cocoapods/synthetic/IOS/Pods/WebRTC-SDK")
        .resolveArchPath(konanTarget, "WebRTC")
    compilations.getByName("main") {
        cinterops.getByName("WebRTC") {
            compilerOpts("-framework", "WebRTC", "-F$webRtcFrameworkPath")
        }
    }

    binaries {
        getTest("DEBUG").apply {
            linkerOpts(
                "-framework",
                "WebRTC",
                "-F$webRtcFrameworkPath",
                "-rpath",
                "$webRtcFrameworkPath",
                "-ObjC"
            )
        }
    }
}
