import org.jetbrains.kotlin.gradle.plugin.mpp.apple.XCFramework
import org.jetbrains.kotlin.konan.target.KonanTarget

plugins {
    id("multiplatform-setup")
}

fun firebaseArchVariant(target: KonanTarget): String {
    return if (target is KonanTarget.IOS_X64 || target is KonanTarget.IOS_SIMULATOR_ARM64) {
        "ios-arm64_i386_x86_64-simulator"
    } else {
        "ios-arm64_armv7"
    }
}

kotlin {
    val xcf = XCFramework()

    val firebaseCoreFrameworks = listOf(
        "FirebaseAnalytics",
        "FirebaseCore",
        "FirebaseCoreDiagnostics",
        "FirebaseInstallations",
        "GoogleAppMeasurement",
        "GoogleAppMeasurementIdentitySupport",
        "GoogleDataTransport",
        "GoogleUtilities",
        "nanopb",
        "PromisesObjC",
    )

    val firestoreFrameworks = listOf(
        "abseil",
        "BoringSSL-GRPC",
        "FirebaseFirestore",
        "gRPC-C++",
        "gRPC-Core",
        "leveldb-library",
        "Libuv-gRPC"
    )

    ios {

        compilations.getByName("main") {
            cinterops.create("FirebaseCore") {
                firebaseCoreFrameworks.forEach { framework ->
                    compilerOpts("-framework", framework, "-F${resolveFrameworkPathCart(framework, ::firebaseArchVariant)}")
                }
            }

            cinterops.create("FirebaseFirestore") {
                firestoreFrameworks.forEach { framework ->
                    compilerOpts("-framework", framework, "-F${resolveFrameworkPathCart(framework, ::firebaseArchVariant)}")
                }
            }
        }

        binaries.framework {
            baseName = "shared"
            xcf.add(this)

            export(project(":webrtc-kmp"))
            export(deps.decompose)
            transitiveExport = true
            isStatic = true

            (firebaseCoreFrameworks + firestoreFrameworks).forEach {
                linkerOpts("-framework", it, "-F${resolveFrameworkPathCart(it, ::firebaseArchVariant)}")
            }
            linkerOpts("-framework", "WebRTC", "-F${resolveFrameworkPath("WebRTC")}", "-ObjC")

            embedBitcode("disable")
        }
    }
}

dependencies {
    commonMainApi(project(":webrtc-kmp"))
    commonMainApi(deps.decompose)
    commonMainImplementation(deps.kotlin.coroutines)
    commonMainImplementation(deps.kermit)
    androidMainImplementation(platform(deps.firebase.bom))
    androidMainImplementation(deps.firebase.firestore)
    androidMainImplementation(deps.kotlin.coroutinesPlayServices)
    jsMainImplementation(npm("firebase", version = "9.9.1"))
}
