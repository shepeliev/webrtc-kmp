import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.konan.target.KonanTarget
import java.io.File

val KonanTarget.xcFrameworkArchs
    get() = when (this) {
        is KonanTarget.IOS_X64,
        is KonanTarget.IOS_SIMULATOR_ARM64 -> listOf("ios-x86_64-simulator", "ios-arm64_x86_64-simulator", "ios-arm64_i386_x86_64-simulator")
        is KonanTarget.MACOS_X64 -> listOf("macos-x86_64", "macos-arm64_x86_64-simulator")
        is KonanTarget.IOS_ARM64 -> listOf("ios-arm64", "ios-arm64_armv7")
        else -> throw IllegalArgumentException("Can't map target '$this' to xCode framework architecture")
    }

fun KotlinNativeTarget.resolveFrameworkPath(frameworkName: String): File {
    val frameworksPath = project.rootDir.resolve("vendor/apple")
    val archs = konanTarget.xcFrameworkArchs
    return archs.map { frameworksPath.resolve("$frameworkName.xcframework/$it") }.first { it.exists() }
}
