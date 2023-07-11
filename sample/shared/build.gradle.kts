import org.jetbrains.kotlin.gradle.plugin.mpp.apple.XCFramework
import org.jetbrains.kotlin.gradle.plugin.mpp.apple.XCFrameworkConfig

plugins {
    id("multiplatform-setup")
}

kotlin {
    val xcf = XCFramework()

    ios { configureIos(xcf) }
    iosSimulatorArm64 { configureIos(xcf) }

    sourceSets {
        val iosMain by getting
        val iosSimulatorArm64Main by getting
        iosSimulatorArm64Main.dependsOn(iosMain)
    }
}

android {
    namespace = "com.shepeliev.webrtckmp.sample.shared"
}

dependencies {
    commonMainApi(project(":webrtc-kmp"))
    commonMainApi(deps.decompose)
    commonMainImplementation(deps.kotlin.coroutines)
    commonMainImplementation(deps.kermit)
    androidMainImplementation(platform(deps.firebase.bom))
    androidMainImplementation(deps.firebase.firestore)
    androidMainImplementation(deps.kotlin.coroutinesPlayServices)
    jsMainImplementation(npm("firebase", version = "9.9.1"))
}

fun org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget.configureIos(xcf: XCFrameworkConfig) {
    val frameworks = getFrameworks(konanTarget).filterKeys { it != "WebRTC" }

    compilations.getByName("main") {
        cinterops.create("FirebaseCore") {
            frameworks.forEach { (framework, path) ->
                compilerOpts("-framework", framework, "-F$path")
            }
        }

        cinterops.create("FirebaseFirestore") {
            frameworks.forEach { (framework, path) ->
                compilerOpts("-framework", framework, "-F$path")
            }
        }
    }

    binaries.framework {
        baseName = "shared"
        xcf.add(this)

        export(project(":webrtc-kmp"))
        export(deps.decompose)
        transitiveExport = true
        isStatic = true

        frameworks.forEach { (framework, path) ->
            linkerOpts("-framework", framework, "-F$path")
        }

        linkerOpts("-ObjC")
    }
}
