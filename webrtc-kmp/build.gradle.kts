plugins {
    id("multiplatform-setup")
    id("maven-publish")
    id("signing")
}

group = "com.shepeliev"

val webRtcKmpVersion: String by properties
version = webRtcKmpVersion

kotlin {
    android {
        publishAllLibraryVariants()
    }

    ios {
        val frameworks = getFrameworks(konanTarget).filterKeys { it == "WebRTC" }

        compilations.getByName("main") {
            cinterops.create("WebRTC") {
                frameworks.forEach { (framework, path) ->
                    compilerOpts("-framework", framework, "-F$path")
                }
            }
        }

        binaries {
            getTest("DEBUG").apply {
                frameworks.forEach { (framework, path) ->
                    linkerOpts("-framework", framework, "-F$path", "-rpath", "$path", "-ObjC")
                }
            }
        }
    }
}

android {
    namespace = "com.shepeliev.webrtckmp"

    defaultConfig {
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
}

dependencies {
    commonMainImplementation(deps.kotlin.coroutines)
    androidMainImplementation(deps.androidx.coreKtx)
    androidMainApi(deps.webrtcSdk)
    androidTestImplementation(deps.androidx.test.core)
    androidTestImplementation(deps.androidx.test.runner)
    jsMainImplementation(npm("webrtc-adapter", "8.1.1"))
}

publishing {
    publications.all {
        this as MavenPublication

        pom {
            name.set(project.name)
            description.set("WebRTC Kotlin Multiplatform SDK")
            url.set("https://github.com/shepeliev/webrtc-kmp")

            scm {
                url.set("https://github.com/shepeliev/webrtc-kmp")
                connection.set("scm:git:https://github.com/shepeliev/webrtc-kmp.git")
                developerConnection.set("scm:git:https://github.com/shepeliev/webrtc-kmp.git")
                tag.set("HEAD")
            }

            issueManagement {
                system.set("GitHub Issues")
                url.set("https://github.com/shepeliev/webrtc-kmp/issues")
            }

            developers {
                developer {
                    name.set("Alex Shepeliev")
                    email.set("a.shepeliev@gmail.com")
                }
            }

            licenses {
                license {
                    name.set("The Apache Software License, Version 2.0")
                    url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                    distribution.set("repo")
                    comments.set("A business-friendly OSS license")
                }
            }
        }
    }
}

signing {
    val signingKey: String by extra
    val signingPassword: String by extra

    useInMemoryPgpKeys(signingKey, signingPassword)
    sign(publishing.publications)
}
