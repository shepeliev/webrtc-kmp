import org.jetbrains.kotlin.gradle.plugin.mpp.apple.XCFramework
import org.jetbrains.kotlin.konan.target.KonanTarget

plugins {
    id("multiplatform-setup")
}

val KonanTarget.xcFrameworkArch get() = when (this) {
    is KonanTarget.IOS_X64,
    is KonanTarget.IOS_SIMULATOR_ARM64 -> "ios-x86_64-simulator"
    is KonanTarget.MACOS_X64 -> "macos-x86_64"
    is KonanTarget.IOS_ARM64 -> "ios-arm64"
    else -> throw IllegalArgumentException("Can't map target '$this' to xCode framework architecture")
}

kotlin {
    val xcf = XCFramework()
    val webRtcFrameworkPath = rootDir.resolve("vendor/apple/WebRTC.xcframework")

    ios {
        binaries.framework {
            baseName = "shared"
            xcf.add(this)

            export(project(":webrtc-kmp"))
            export(deps.decompose)
            transitiveExport = true

            val arch = konanTarget.xcFrameworkArch
            linkerOpts("-framework", "WebRTC", "-F${webRtcFrameworkPath.resolve(arch)}")
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
