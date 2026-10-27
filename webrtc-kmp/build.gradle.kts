@file:OptIn(ExperimentalKotlinGradlePluginApi::class)

import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSetTree
import org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpackConfig

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidKotlinMultiplatformLibrary)
    alias(libs.plugins.vanniktech.maven.publish)
}

group = "com.shepeliev"

version = providers.environmentVariable("VERSION").getOrElse("0.0.0")

kotlin {
    explicitApi()
    jvmToolchain(libs.versions.jvmToolchain.get().toInt())

    compilerOptions {
        freeCompilerArgs.add("-Xexpect-actual-classes")
    }

    swiftPMDependencies {
        swiftPackage(
            url = url("https://github.com/webrtc-sdk/Specs.git"),
            version = exact(libs.versions.webrtc.ios.sdk.get()),
            products = listOf(product("WebRTC")),
        )
    }

    android {
        namespace = "com.shepeliev.webrtckmp"
        compileSdk =
            libs.versions.compileSdk
                .get()
                .toInt()
        minSdk =
            libs.versions.minSdk
                .get()
                .toInt()

        withDeviceTestBuilder {
            sourceSetTreeName = KotlinSourceSetTree.test.name
        }.configure {
            instrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        }

        compilerOptions {
            jvmTarget = JvmTarget.JVM_1_8
        }
    }

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
                devServer =
                    (devServer ?: KotlinWebpackConfig.DevServer()).apply {
                        // Serve sources to debug inside browser
                        static(project.rootDir.path)
                    }
            }
        }
    }

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
            api(libs.webrtc.sdk)
            implementation(libs.kotlin.coroutines.android)
            implementation(libs.androidx.coreKtx)
            implementation(libs.androidx.startup)
        }

        jsMain.dependencies {
            implementation(npm("webrtc-adapter", libs.versions.webrtc.adapter.get()))
        }

        wasmJsMain.dependencies {
            implementation(libs.kotlin.browser)
        }

        commonTest.dependencies {
            implementation(kotlin("test"))
            implementation(libs.kotlin.coroutines.test)
        }

        getByName("androidDeviceTest").dependencies {
            implementation(libs.androidx.test.core)
            implementation(libs.androidx.test.runner)
            implementation(libs.androidx.test.rules)
        }

        named { it.startsWith("ios") }.configureEach {
            languageSettings.optIn("kotlinx.cinterop.ExperimentalForeignApi")
        }
    }
}

mavenPublishing {
    publishToMavenCentral()
    signAllPublications()

    coordinates(project.group.toString(), project.name, "${project.version}")

    pom {
        name = "WebRTC KMP"
        description = "WebRTC Kotlin Multiplatform SDK."
        url = "https://github.com/shepeliev/webrtc-kmp"

        licenses {
            license {
                name = "The Apache License, Version 2.0"
                url = "http://www.apache.org/licenses/LICENSE-2.0.txt"
                distribution = "http://www.apache.org/licenses/LICENSE-2.0.txt"
            }
        }

        developers {
            developer {
                id = "shepeliev"
                name = "Oleksandr Shepeliev"
                email = "a.shepeliev@gmail.com"
            }
        }

        scm {
            url = "https://github.com/shepeliev/webrtc-kmp"
            connection = "scm:git:https://github.com/shepeliev/webrtc-kmp.git"
            developerConnection = "scm:git:https://github.com/shepeliev/webrtc-kmp.git"
        }
    }
}
