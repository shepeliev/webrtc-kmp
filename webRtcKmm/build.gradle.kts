import org.apache.tools.ant.taskdefs.condition.Os
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.gradle.tasks.CInteropProcess

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
        val frameworksPath = "${projectDir}/src/nativeInterop/cinterop/Carthage/Build/iOS"

        binaries {
            getTest(DEBUG).apply {
                linkerOpts("-F$frameworksPath", "-rpath", frameworksPath)
            }
        }

        compilations.getByName("main") {
            cinterops.create("WebRTC") {
                compilerOpts("-F$frameworksPath")
            }
        }

    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.4.2")
            }
        }

        val commonTest by getting {
            dependencies {
                implementation(kotlin("test-common"))
                implementation(kotlin("test-annotations-common"))
            }
        }

        val androidMain by getting {
            dependencies {
                api("org.webrtc:google-webrtc:1.0.32006")
            }
        }

        val iosMain by getting

        val iosTest by getting
    }
}

android {
    compileSdkVersion(30)
    defaultConfig {
        minSdkVersion(24)
        targetSdkVersion(30)
    }
    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
    sourceSets["main"].res.srcDir("src/androidMain/res")

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

//val packForXcode by tasks.creating(Sync::class) {
//    group = "build"
//    val mode = System.getenv("CONFIGURATION") ?: "DEBUG"
//    val sdkName = System.getenv("SDK_NAME") ?: "iphonesimulator"
////    val targetName = "ios" + if (sdkName.startsWith("iphoneos")) "Arm64" else "X64"
//    val targetName = "ios"
//    val framework =
//        kotlin.targets.getByName<KotlinNativeTarget>(targetName).binaries.getFramework(mode)
//    inputs.property("mode", mode)
//    dependsOn(framework.linkTask)
//    val targetDir = File(buildDir, "xcode-frameworks")
//    from({ framework.outputDirectory })
//    into(targetDir)
//}

tasks {
//    getByName("build").dependsOn(packForXcode)

    val carthageBootstrap by creating(Exec::class) {
        group = "carthage"
        commandLine(
            "carthage",
            "update",
            "--platform", "iOS",
            "--project-directory", "${projectDir}/src/nativeInterop/cinterop/",
            "--cache-builds"
        )
    }

    if (Os.isFamily(Os.FAMILY_MAC)) {
        withType(CInteropProcess::class) {
            dependsOn("carthageBootstrap")
        }
    }

    create("carthageClean", Delete::class.java) {
        group = "carthage"
        delete(
            projectDir.resolve("src/nativeInterop/cinterop/Carthage"),
            projectDir.resolve("src/nativeInterop/cinterop/Cartfile.resolved")
        )
    }
}
