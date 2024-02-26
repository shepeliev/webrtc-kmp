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
    jvm()
    ios { configureIos() }
    iosSimulatorArm64 { configureIos() }

    sourceSets {
        val iosMain by getting
        val iosSimulatorArm64Main by getting
        iosSimulatorArm64Main.dependsOn(iosMain)

        val iosTest by getting
        val iosSimulatorArm64Test by getting
        iosSimulatorArm64Test.dependsOn(iosTest)
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
    androidMainApi(deps.webrtc.android)
    androidTestImplementation(deps.androidx.test.core)
    androidTestImplementation(deps.androidx.test.runner)

    jvmMainApi(deps.webrtc.java)
    jvmMainImplementation(deps.bouncyCastle)

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
    val hostArch = when(val arch = System.getProperty("os.arch").lowercase()) {
        "amd64" -> "x86_64"
        else -> arch
    }
    jvmTestImplementation(
        group = deps.webrtc.java.get().group!!,
        name = deps.webrtc.java.get().name,
        version = deps.webrtc.java.get().version,
        classifier = "$hostOS-$hostArch"
    )

    jsMainImplementation(npm("webrtc-adapter", "8.1.1"))
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
