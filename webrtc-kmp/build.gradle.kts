import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSetTree
import org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpackConfig

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    kotlin("native.cocoapods")
    id("maven-publish")
    id("signing")
}

group = "com.shepeliev"

version = System.getenv("VERSION") ?: "0.0.0"

kotlin {
    @OptIn(ExperimentalKotlinGradlePluginApi::class)
    compilerOptions {
        freeCompilerArgs.add("-Xexpect-actual-classes")
    }

    cocoapods {
        version = project.version.toString()
        summary = "WebRTC Kotlin Multiplatform SDK"
        homepage = "https://github.com/shepeliev/webrtc-kmp"
        ios.deploymentTarget = "13.0"

        noPodspec()

        pod("WebRTC-SDK") {
            version = libs.versions.webrtc.ios.sdk.get()
            moduleName = "WebRTC"
            packageName = "WebRTC"

            // workaround for https://youtrack.jetbrains.com/issue/KT-69094
            extraOpts += listOf("-compiler-option", "-ivfsoverlay", "-compiler-option", "../vfsoverlay/overlay.yaml")
        }
    }

    @OptIn(ExperimentalKotlinGradlePluginApi::class)
    androidTarget {
        publishAllLibraryVariants()
        instrumentedTestVariant {
            sourceSetTree.set(KotlinSourceSetTree.test)
        }
        compilerOptions {
            jvmTarget = JvmTarget.JVM_1_8
        }
    }
    jvm()
    iosX64()
    iosArm64()
    iosSimulatorArm64()

    js {
        useCommonJs()
        browser()
    }

    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        binaries.executable()
        browser {
            commonWebpackConfig {
                devServer = (devServer ?: KotlinWebpackConfig.DevServer()).apply {
                    static = (static ?: mutableListOf()).apply {
                        // Serve sources to debug inside browser
                        add(project.rootDir.path)
                    }
                }
            }
        }
    }

    @OptIn(ExperimentalKotlinGradlePluginApi::class)
    applyDefaultHierarchyTemplate {
        common {
            group("jsAndWasmJs") {
                withJs()
                withWasmJs()
            }
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(libs.kotlin.coroutines)
        }

        androidMain.dependencies {
            api(libs.webrtc.android)
            implementation(libs.kotlin.coroutines.android)
            implementation(libs.androidx.coreKtx)
            implementation(libs.androidx.startup)
        }

        jsMain.dependencies {
            implementation(npm("webrtc-adapter", "8.1.1"))
        }

        commonTest.dependencies {
            implementation(kotlin("test"))
            implementation(kotlin("test-annotations-common"))
            implementation(libs.kotlin.coroutines.test)
        }

        jvmMain.dependencies {
            api(libs.webrtc.java)
            implementation(libs.java.bouncycastle)
        }
        jvmTest.dependencies {
            val osName = System.getProperty("os.name")
            val hostOS = when {
                osName == "Mac OS X" -> "macos"
                osName.startsWith("Win") -> "windows"
                osName.startsWith("Linux") -> "linux"
                else -> error("Unsupported OS: $osName")
            }
            val hostArch = when (val arch = System.getProperty("os.arch").lowercase()) {
                "amd64" -> "x86_64"
                else -> arch
            }
            implementation("${libs.webrtc.java.get()}:$hostOS-$hostArch")
        }

        val iosX64AndSimulatorArm64Main by creating {
            dependsOn(iosMain.get())
        }

        val iosX64Main by getting
        iosX64Main.dependsOn(iosX64AndSimulatorArm64Main)
        val iosSimulatorArm64Main by getting
        iosSimulatorArm64Main.dependsOn(iosX64AndSimulatorArm64Main)
    }
}

android {
    namespace = "com.shepeliev.webrtckmp"

    compileSdk = libs.versions.compileSdk.get().toInt()
    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
    sourceSets["main"].res.srcDir("src/androidMain/res")

    defaultConfig {
        minSdk = libs.versions.minSdk.get().toInt()
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    testOptions {
        targetSdk = libs.versions.targetSdk.get().toInt()
    }

    dependencies {
        androidTestImplementation(libs.androidx.test.core)
        androidTestImplementation(libs.androidx.test.runner)
    }
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
    val signingKey: String by rootProject.extra
    val signingPassword: String by rootProject.extra

    useInMemoryPgpKeys(signingKey, signingPassword)
    sign(publishing.publications)
}
