import org.jetbrains.kotlin.gradle.plugin.mpp.apple.XCFramework

plugins {
    id("multiplatform-setup")
}

kotlin {
    val xcf = XCFramework()

    ios {
        compilations.getByName("main") {
            cinterops.create("FirebaseCore") {
                getFrameworks(konanTarget).forEach { (framework, path) ->
                    compilerOpts("-framework", framework, "-F$path")
                }
            }

            cinterops.create("FirebaseFirestore") {
                getFrameworks(konanTarget).forEach { (framework, path) ->
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

            getFrameworks(konanTarget).forEach  { (framework, path) ->
                linkerOpts("-framework", framework, "-F$path")
            }

            linkerOpts("-ObjC")
        }
    }
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
