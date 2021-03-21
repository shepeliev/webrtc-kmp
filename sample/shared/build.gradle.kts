import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget


plugins {
    kotlin("multiplatform")
    id("com.android.library")
}

kotlin {
    android()

    val iosTarget: (String, KotlinNativeTarget.() -> Unit) -> KotlinNativeTarget =
        if (project.extra["ideaActive"] as Boolean)
            ::iosX64
        else
            ::iosArm64

    iosTarget("ios") {
        binaries {
            framework {
                baseName = "shared"
                freeCompilerArgs = freeCompilerArgs + "-Xobjc-generics"
                linkerOpts("-F$projectDir/src/nativeInterop/Carthage/Build/iOS")

                export(project(":webRtcKmm"))
            }
        }
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                api(project(":webRtcKmm"))
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.4.2")
            }
        }
        val commonTest by getting
        val androidMain by getting
        val androidTest by getting
        val iosMain by getting
        val iosTest by getting
    }
}

android {
    compileSdkVersion(30)
    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
    defaultConfig {
        minSdkVersion(24)
        targetSdkVersion(30)
    }
}

val packForXcode by tasks.creating(Sync::class) {
    group = "build"
    val mode = System.getenv("CONFIGURATION") ?: "DEBUG"
    val sdkName = System.getenv("SDK_NAME") ?: "iphonesimulator"
//    val targetName = "ios" + if (sdkName.startsWith("iphoneos")) "Arm64" else "X64"
    val targetName = "ios"
    val framework = kotlin.targets.getByName<KotlinNativeTarget>(targetName).binaries.getFramework(mode)
    inputs.property("mode", mode)
    dependsOn(framework.linkTask)
    val targetDir = File(buildDir, "xcode-frameworks")
    from({ framework.outputDirectory })
    into(targetDir)
}

tasks.getByName("build").dependsOn(packForXcode)

val carthageBootstrap by tasks.creating(Exec::class) {
    group = "carthage"
    commandLine(
        "carthage",
        "update",
        "--platform", "iOS",
        "--project-directory", "$projectDir/src/nativeInterop/",
        "--cache-builds"
    )
}

afterEvaluate {
    tasks.named("linkDebugTestIos").configure {
        dependsOn(tasks.named("carthageBootstrap"))
    }
    tasks.named("linkDebugFrameworkIos").configure {
        dependsOn(tasks.named("carthageBootstrap"))
    }
    tasks.named("linkReleaseFrameworkIos").configure {
        dependsOn(tasks.named("carthageBootstrap"))
    }
}