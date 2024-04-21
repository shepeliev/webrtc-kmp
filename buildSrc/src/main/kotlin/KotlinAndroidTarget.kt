import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinAndroidTarget

fun KotlinAndroidTarget.configureJvmTarget(jvmVersion: String = "1.8") {
    compilations.all {
        kotlinOptions.jvmTarget = jvmVersion
    }
}
