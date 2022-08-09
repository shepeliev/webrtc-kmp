import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.konan.target.KonanTarget
import java.io.File

fun firebaseArchVariant(target: KonanTarget): String {
    return if (target is KonanTarget.IOS_X64 || target is KonanTarget.IOS_SIMULATOR_ARM64) {
        "ios-arm64_i386_x86_64-simulator"
    } else {
        "ios-arm64_armv7"
    }
}

fun webrtcArchVariant(target: KonanTarget): String {
    return if (target is KonanTarget.IOS_X64 || target is KonanTarget.IOS_SIMULATOR_ARM64) {
        "ios-x86_64-simulator"
    } else {
        "ios-arm64"
    }
}

fun KotlinNativeTarget.resolveFrameworkPath(frameworkName: String, resolveArch: (KonanTarget) -> String): File {
    val frameworksPath = project.projectDir.resolve("src/nativeInterop/cinterop/Carthage/Build")
    return frameworksPath.resolve("$frameworkName.xcframework/${resolveArch(konanTarget)}")
}
