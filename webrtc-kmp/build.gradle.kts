plugins {
    id("com.android.library")
    kotlin("multiplatform") version "1.6.0"
    id("org.jmailen.kotlinter") version "3.4.4"
    id("publish-setup")
}

kotlin {
    android {
        publishAllLibraryVariants()
    }

    ios {
        val webRtcFrameworkPath = rootDir.resolve("libs/ios/WebRTC.xcframework/ios-x86_64-simulator/")
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
                    extraOpts = listOf("-compiler-option", "-DNS_FORMAT_ARGUMENT(A)=", "-verbose")
                }
            }
        }
    }

    js {
        useCommonJs()
        browser {
            testTask {
                useKarma {
                    useChromeHeadless()
                }
            }
        }
    }

    sourceSets {
        val coroutinesVersion = "1.6.0-native-mt"

        val commonMain by getting {
            dependencies {
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutinesVersion")
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
                implementation("androidx.core:core:1.7.0")
                api(fileTree("../libs/android") { include("*.jar") })
            }
        }

        val androidTest by getting {
            dependencies {
                implementation(kotlin("test-junit"))
                implementation("junit:junit:4.13.2")
                implementation("androidx.test:core:1.4.0")
                implementation("androidx.test.ext:junit:1.1.3")
                implementation("androidx.test:runner:1.4.0")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:$coroutinesVersion")
            }
        }

        val jsMain by getting {
            dependencies {
                implementation(npm("webrtc-adapter", "8.0.0"))
                implementation(kotlin("stdlib-js"))
            }
        }

        val jsTest by getting {
            dependencies {
                implementation(kotlin("test-js"))
            }
        }
    }
}

android {
    compileSdk = 31
    defaultConfig {
        minSdk = 21
        targetSdk = 31
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
}
