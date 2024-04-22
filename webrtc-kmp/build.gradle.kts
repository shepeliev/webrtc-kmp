import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget

plugins {
    id("webrtc.multiplatform")
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
        ios.deploymentTarget = "13.0"

        pod("WebRTC-SDK") {
            version = libs.versions.webrtc.ios.sdk.get()
            moduleName = "WebRTC"
            packageName = "WebRTC"
        }
    }

    androidTarget {
        publishAllLibraryVariants()
    }
    jvm()
    iosX64 { configureWebRtcCinterops() }
    iosArm64 { configureWebRtcCinterops() }
    iosSimulatorArm64 { configureWebRtcCinterops() }

    js {
        useCommonJs()
        browser()
    }

    sourceSets {
        commonMain.dependencies {
            implementation(libs.kotlin.coroutines)
        }

        androidMain.dependencies {
            api(libs.webrtc.android)
            implementation(libs.kotlin.coroutines.android)
            implementation(libs.androidx.coreKtx)
            implementation(libs.androidx.startup)
        }

        jsMain.dependencies {
            implementation(npm("webrtc-adapter", "8.1.1"))
        }

        commonTest.dependencies {
            implementation(kotlin("test"))
            implementation(kotlin("test-annotations-common"))
        }

        jvmMain.dependencies {
            api(libs.webrtc.java)
            implementation(libs.java.bouncycastle)

            val osName = System.getProperty("os.name").lowercase()
            val hostOS = if (osName.contains("mac")) {
                "macos"
            } else if (osName.contains("linux")) {
                "linux"
            } else if (osName.contains("windows")) {
                "windows"
            } else {
                throw IllegalStateException("Unsupported OS: $osName")
            }
            val hostArch = when (val arch = System.getProperty("os.arch").lowercase()) {
                "amd64" -> "x86_64"
                else -> arch
            }
            implementation("${libs.webrtc.java.get()}:$hostOS-$hostArch")
        }
    }
}

android {
    namespace = "com.shepeliev.webrtckmp"

    defaultConfig {
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    dependencies {
        androidTestImplementation(libs.androidx.test.core)
        androidTestImplementation(libs.androidx.test.runner)
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
    val signingKey: String by rootProject.extra
    val signingPassword: String by rootProject.extra

    useInMemoryPgpKeys(signingKey, signingPassword)
    sign(publishing.publications)
}

fun KotlinNativeTarget.configureWebRtcCinterops() {
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
