import org.jetbrains.kotlin.gradle.tasks.CInteropProcess

plugins {
    id("com.android.library")
    kotlin("multiplatform")
    id("org.jlleitschuh.gradle.ktlint")
    id("de.undercouch.download")
}

kotlin {
    android()

    js {
        useCommonJs()
        browser()
    }

    sourceSets {
        getByName("commonTest") {
            dependencies {
                implementation(kotlin("test"))
                implementation(kotlin("test-annotations-common"))
            }
        }
    }
}

@Suppress("UnstableApiUsage")
android {
    compileSdk = AndroidConfig.compileSdkVersion

    sourceSets.named("main") {
        manifest.srcFile("src/androidMain/AndroidManifest.xml")
        res.srcDir("src/androidMain/res")
    }

    defaultConfig {
        minSdk = AndroidConfig.minSdkVersion
        targetSdk = AndroidConfig.targetSdkVersion
    }
}

if (projectDir.resolve("src/nativeInterop/cinterop/Cartfile").exists()) {
    tasks.register<Exec>("carthageUpdate") {
        group = "Carthage"
        description = "Update Carthage dependencies"
        executable = "carthage"
        args(
            "update",
            "--project-directory",
            projectDir.resolve("src/nativeInterop/cinterop"),
            "--use-xcframeworks",
            "--cache-builds"
        )
    }

    tasks.withType<CInteropProcess>() {
        dependsOn("carthageUpdate")
    }
}

tasks.register<Delete>("carthageClean") {
    group = "Carthage"
    description = "Clean Carthage dependencies"
    delete(
        projectDir.resolve("src/nativeInterop/cinterop/Carthage"),
        projectDir.resolve("src/nativeInterop/cinterop/Cartfile.resolved")
    )
}

afterEvaluate {
    tasks.named<Delete>("clean") {
        dependsOn("carthageClean")
    }
}
