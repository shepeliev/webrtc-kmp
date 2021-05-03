
plugins {
    id("com.android.library")
    kotlin("multiplatform") version "1.4.31"
    kotlin("native.cocoapods") version "1.4.31"
    id("maven-publish")
}

group = "com.shepeliev"
version = "1.0-alpha01"

repositories {
    google()
    jcenter()
    mavenCentral()
}


kotlin {
    cocoapods {
        ios.deploymentTarget = "11.0"
        summary = "WebRTC Kotlin Multiplatform"
        homepage = "https://github.com/shepeliev/webrtc-kmp"

        pod("GoogleWebRTC") {
            moduleName = "WebRTC"
            packageName = "WebRTC"
        }
    }

    android {
        publishLibraryVariants("release", "debug")
    }

    val iosArm64 = iosArm64()
    val iosX64 = iosX64("ios") {
        val webRtcFrameworkPath = "$buildDir/cocoapods/synthetic/IOS/${project.name.replace("-", "_")}/Pods/GoogleWebRTC/Frameworks/frameworks"
        binaries.getTest(DEBUG).apply {
            linkerOpts("-F$webRtcFrameworkPath", "-framework", "WebRTC", "-rpath", webRtcFrameworkPath)
        }
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.4.3-native-mt")
            }
        }

        val commonTest by getting {
            dependencies {
                implementation(kotlin("test-common"))
                implementation(kotlin("test-annotations-common"))
            }
        }

        val androidMain by getting {
            dependencies {
                implementation("androidx.core:core:1.3.2")
                api("org.webrtc:google-webrtc:1.0.32006")
            }
        }

        val androidTest by getting {
            dependencies {
                implementation(kotlin("test-junit"))
                implementation("junit:junit:4.13")
                implementation("androidx.test:core:1.3.0")
                implementation("androidx.test.ext:junit:1.1.2")
                implementation("androidx.test:runner:1.3.0")
            }
        }

        configure(listOf(iosArm64, iosX64)) {
            compilations.getByName("main") {
                source(sourceSets.get("iosMain"))
            }
        }

//        val iosMain by getting
//        val iosTest by getting

//        val iosX64Main by getting {
//            kotlin.srcDir(file("src/iosMain/kotlin"))
//        }
//
//        val iosX64Test by getting {
//            kotlin.srcDir(file("src/iosTest/kotlin"))
//        }
//
//        val iosArm64Main by getting {
//            dependsOn(iosMain)
//        }
    }
}

android {
    compileSdkVersion(30)
    defaultConfig {
        minSdkVersion(21)
        targetSdkVersion(30)
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
//    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")

    sourceSets {
        getByName("main") {
            manifest.srcFile("src/androidMain/AndroidManifest.xml")
        }
        getByName("androidTest"){
            java.srcDir(file("src/androidTest/kotlin"))
//            manifest.srcFile("src/androidTest/AndroidManifest.xml")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().all {
    kotlinOptions {
        jvmTarget = "1.8"
    }
}
