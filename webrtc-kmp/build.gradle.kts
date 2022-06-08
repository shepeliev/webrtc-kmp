plugins {
    id("multiplatform-setup")
    id("publish-setup")
}

group = "com.shepeliev"
version = "0.89.7"

kotlin {
    android {
        publishAllLibraryVariants()
    }

//    ios {
//        val webRtcFrameworkPath = rootDir.resolve("libs/ios/WebRTC.xcframework/ios-x86_64-simulator/")
//        binaries {
//            getTest("DEBUG").apply {
//                linkerOpts(
//                    "-F$webRtcFrameworkPath",
//                    "-rpath",
//                    "$webRtcFrameworkPath"
//                )
//            }
//            compilations.getByName("main") {
//                cinterops.create("WebRTC") {
//                    compilerOpts("-F$webRtcFrameworkPath")
//                    extraOpts = listOf("-compiler-option", "-DNS_FORMAT_ARGUMENT(A)=", "-verbose")
//                }
//            }
//        }
//    }

//    js {
//        useCommonJs()
//        browser {
//            testTask {
//                useKarma {
//                    useChromeHeadless()
//                }
//            }
//        }
//    }

    sourceSets {
//        val androidTest by getting {
//            dependencies {
//                implementation(kotlin("test-junit"))
//                implementation("junit:junit:4.13.2")
//                implementation("androidx.test:core:1.4.0")
//                implementation("androidx.test.ext:junit:1.1.3")
//                implementation("androidx.test:runner:1.4.0")
//                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:$coroutinesVersion")
//            }
//        }

//        val jsMain by getting {
//            dependencies {
//                implementation(npm("webrtc-adapter", "8.0.0"))
//                implementation(kotlin("stdlib-js"))
//            }
//        }

//        val jsTest by getting {
//            dependencies {
//                implementation(kotlin("test-js"))
//            }
//        }
    }
}

dependencies {
    commonMainImplementation(deps.kotlin.coroutines)
    androidMainImplementation(deps.androidx.coreKtx)
    androidMainApi(fileTree("../libs/android") { include("*.jar") })
}
