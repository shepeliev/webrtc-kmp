import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget

plugins {
    id("com.android.library")
    kotlin("multiplatform") version "1.4.31"
    id("maven-publish")
}

group = "com.shepeliev"
version = "1.0-alpha02"

repositories {
    google()
    mavenCentral()
}


kotlin {
    android {
        publishAllLibraryVariants()
    }

    fun configureNativeTarget(): KotlinNativeTarget.() -> Unit = {
        val webRtcFrameworkPath = projectDir.resolve("framework/WebRTC.xcframework/ios-x86_64-simulator/")
        binaries {
            getTest("DEBUG").apply {
                linkerOpts(
                    "-F$webRtcFrameworkPath",
                    "-rpath",
                    "$webRtcFrameworkPath"
                )
            }
            compilations.getByName("main") {
                cinterops.create("WebRTC") {
                    compilerOpts("-F$webRtcFrameworkPath")
                }
            }
        }
    }

    val iosX64 = iosX64("ios", configureNativeTarget())
    val iosArm64 = iosArm64(configure = configureNativeTarget())

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
                api(fileTree("libs") { include("*.jar") })
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
                source(sourceSets["iosMain"])
            }
        }
    }
}

android {
    compileSdkVersion(30)
    defaultConfig {
        minSdkVersion(21)
        targetSdkVersion(30)
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    sourceSets {
        getByName("main") {
            manifest.srcFile("src/androidMain/AndroidManifest.xml")
        }
        getByName("androidTest") {
            java.srcDir(file("src/androidTest/kotlin"))
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
