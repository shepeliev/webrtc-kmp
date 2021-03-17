import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget


plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization") version "1.4.31"
    id("com.android.library")
}

version = "1.0.0"
val mviKotlinVersion = "2.0.1"

kotlin {
    android()

    val iosTarget: (String, KotlinNativeTarget.() -> Unit) -> KotlinNativeTarget =
        if (System.getenv("SDK_NAME")?.startsWith("iphoneos") == true)
            ::iosArm64
        else
            ::iosX64

    iosTarget("ios") {
        binaries {
            framework {
                baseName = "shared"
                freeCompilerArgs = freeCompilerArgs + "-Xobjc-generics"
                linkerOpts("-F$projectDir/src/nativeInterop/Carthage/Build/iOS")

                export(project(":webRtcKmm"))
                export("com.arkivanov.mvikotlin:mvikotlin-iosx64:$mviKotlinVersion")
                export("com.arkivanov.mvikotlin:mvikotlin-main-iosx64:$mviKotlinVersion")
            }
        }
    }

    sourceSets {
        val ktorVersion = "1.5.1"

        val commonMain by getting {
            dependencies {
                api(project(":webRtcKmm"))
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.4.2-native-mt") {
                    version { strictly("1.4.2-native-mt") }
                }
                implementation("io.ktor:ktor-client-core:$ktorVersion")
                implementation("io.ktor:ktor-client-serialization:$ktorVersion")
                api("com.arkivanov.mvikotlin:mvikotlin:$mviKotlinVersion")
                api("com.arkivanov.mvikotlin:mvikotlin-main:$mviKotlinVersion")
                implementation("com.arkivanov.mvikotlin:mvikotlin-extensions-coroutines:$mviKotlinVersion")
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
                api("io.ktor:ktor-client-okhttp:$ktorVersion")
                api("io.ktor:ktor-client-websockets:$ktorVersion")
                implementation("com.google.android.material:material:1.2.1")
            }
        }
        val androidTest by getting {
            dependencies {
                implementation(kotlin("test-junit"))
                implementation("junit:junit:4.13")
                implementation("androidx.test:core:1.3.0")
                implementation("androidx.test.ext:junit:1.1.2")
                implementation("androidx.test:runner:1.3.0")
            }
        }
        val iosMain by getting {
            dependencies {
                implementation("io.ktor:ktor-client-ios:$ktorVersion")
            }
        }
        val iosTest by getting
    }
}

android {
    compileSdkVersion(30)
    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
    defaultConfig {
        minSdkVersion(24)
        targetSdkVersion(30)
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    testOptions {
        unitTests.apply {
            isIncludeAndroidResources = true
        }
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
        "--project-directory", "${projectDir}/src/nativeInterop/",
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