import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

fun KotlinMultiplatformExtension.configureKotlinCompilerArgs(vararg args: String) {
    targets.all {
        compilations.all {
            kotlinOptions {
                freeCompilerArgs += setOf(
                    "-opt-in=kotlin.RequiresOptIn",
                    "-Xexpect-actual-classes",
                    *args
                )
            }
        }
    }
}
