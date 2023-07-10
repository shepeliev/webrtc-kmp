import org.gradle.api.Project
import org.jetbrains.kotlin.konan.target.KonanTarget
import java.io.File

fun File.resolveArchPath(target: KonanTarget, framework: String): File {
    val archPaths = resolve("$framework.xcframework")
        .listFiles { _, name -> target.matches(name) }
        ?: error("Can't find framework '$framework' for target '${target.name}'")

    check(archPaths.size == 1) { "Resolving framework '$framework' arch path failed: $archPaths" }

    return archPaths.first()
}

private fun KonanTarget.matches(dir: String): Boolean {
    return when (this) {
        KonanTarget.IOS_SIMULATOR_ARM64,
        KonanTarget.IOS_X64 -> dir.startsWith("ios") && dir.endsWith("simulator")

        KonanTarget.IOS_ARM64 -> dir.startsWith("ios-arm64") && !dir.contains("x86")

        else -> error("Unsupported target $name")
    }
}
