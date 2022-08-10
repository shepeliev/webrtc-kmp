import org.jetbrains.kotlin.gradle.plugin.mpp.apple.XCFramework

plugins {
    id("multiplatform-setup")
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
                    compilerOpts(
                        "-framework",
                        framework,
                        "-F${resolveFrameworkPath(framework, ::firebaseArchVariant)}"
                    )
                }
            }

            cinterops.create("FirebaseFirestore") {
                firestoreFrameworks.forEach { framework ->
                    compilerOpts(
                        "-framework",
                        framework,
                        "-F${resolveFrameworkPath(framework, ::firebaseArchVariant)}"
                    )
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
                linkerOpts("-framework", it, "-F${resolveFrameworkPath(it, ::firebaseArchVariant)}")
            }

            val webRtcFrameworkPath = project(":webrtc-kmp").resolveFrameworkPath("WebRTC", ::webrtcArchVariant)
            linkerOpts("-framework", "WebRTC", "-F$webRtcFrameworkPath")

            linkerOpts("-ObjC")
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
