import org.gradle.api.Project
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.konan.target.KonanTarget
import java.io.File

fun KotlinNativeTarget.firebaseArchVariant(): String {
    return if (konanTarget is KonanTarget.IOS_X64 || konanTarget is KonanTarget.IOS_SIMULATOR_ARM64) {
        "ios-arm64_i386_x86_64-simulator"
    } else {
        "ios-arm64_armv7"
    }
}

fun KotlinNativeTarget.webrtcArchVariant(): String {
    return if (konanTarget is KonanTarget.IOS_X64 || konanTarget is KonanTarget.IOS_SIMULATOR_ARM64) {
        "ios-arm64_x86_64-simulator"
    } else {
        "ios-arm64"
    }
}

fun Project.resolveFrameworkPath(frameworkName: String, resolveArch: () -> String): File {
    val frameworksPath = project.projectDir.resolve("src/nativeInterop/cinterop/Carthage/Build")
    return frameworksPath.resolve("$frameworkName.xcframework/${resolveArch()}")
}
