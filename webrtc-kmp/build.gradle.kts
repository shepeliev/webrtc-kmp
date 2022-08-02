import org.jetbrains.kotlin.konan.target.KonanTarget

plugins {
    id("multiplatform-setup")
    id("publish-setup")
}

group = "com.shepeliev"
version = "0.89.7"

val KonanTarget.xcFrameworkArch get() = when (this) {
    is KonanTarget.IOS_X64,
    is KonanTarget.IOS_SIMULATOR_ARM64 -> "ios-x86_64-simulator"
    is KonanTarget.MACOS_X64 -> "macos-x86_64"
    is KonanTarget.IOS_ARM64 -> "ios-arm64"
    else -> throw IllegalArgumentException("Can't map target '$this' to xCode framework architecture")
}

kotlin {
    android {
        publishAllLibraryVariants()
    }

    ios {
        val webRtcFrameworkPath = rootDir.resolve("vendor/apple/WebRTC.xcframework")
        compilations.getByName("main") {
            cinterops.create("WebRTC") {
                val arch = konanTarget.xcFrameworkArch
                compilerOpts("-framework", "WebRTC", "-F${webRtcFrameworkPath.resolve(arch)}")
            }
        }

        binaries.all {
            val arch = konanTarget.xcFrameworkArch
            linkerOpts("-framework", "WebRTC", "-F${webRtcFrameworkPath.resolve(arch)}")
        }
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

//    js(BOTH) {
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
    androidMainApi(fileTree("../vendor/android") { include("*.jar") })
}
