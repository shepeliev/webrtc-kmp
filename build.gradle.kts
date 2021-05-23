import com.android.build.gradle.internal.cxx.configure.gradleLocalProperties
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("com.android.library")
    kotlin("multiplatform") version "1.4.31"
    id("io.github.gradle-nexus.publish-plugin") version "1.1.0"
    id("org.jmailen.kotlinter") version "3.4.4"
    id("maven-publish")
    id("signing")
}

group = "com.shepeliev"
version = "0.89.1"

repositories {
    google()
    mavenCentral()
}

kotlin {
    android {
        publishAllLibraryVariants()
    }

    fun configureNativeTarget(): KotlinNativeTarget.() -> Unit = {
        val webRtcFrameworkPath =
            projectDir.resolve("framework/WebRTC.xcframework/ios-x86_64-simulator/")
        binaries {
            getTest("DEBUG").apply {
                linkerOpts(
                    "-F$webRtcFrameworkPath",
                    "-rpath",
                    "$webRtcFrameworkPath"
                )
            }
            compilations.getByName("main") {
                cinterops.create("WebRTC") {
                    compilerOpts("-F$webRtcFrameworkPath")
                }
            }
        }
    }

    val iosX64 = iosX64("ios", configureNativeTarget())
    val iosArm64 = iosArm64(configure = configureNativeTarget())

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.4.3-native-mt")
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
                implementation("androidx.core:core:1.3.2")
                api(fileTree("libs") { include("*.jar") })
            }
        }

        val androidTest by getting {
            dependencies {
                implementation(kotlin("test-junit"))
                implementation("junit:junit:4.13")
                implementation("androidx.test:core:1.3.0")
                implementation("androidx.test.ext:junit:1.1.2")
                implementation("androidx.test:runner:1.3.0")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.4.3-native-mt")
            }
        }

        configure(listOf(iosArm64, iosX64)) {
            compilations.getByName("main") {
                source(sourceSets["iosMain"])
            }
        }
    }
}

android {
    compileSdkVersion(30)
    defaultConfig {
        minSdkVersion(21)
        targetSdkVersion(30)
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    sourceSets {
        getByName("main") {
            manifest.srcFile("src/androidMain/AndroidManifest.xml")
        }
        getByName("androidTest") {
            java.srcDir(file("src/androidTest/kotlin"))
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

tasks {
    withType<KotlinCompile>().all {
        kotlinOptions {
            jvmTarget = "1.8"
        }
    }

    val updatePodspecVersion by registering(Copy::class) {
        val from = file("webrtc-kmp.podspec")
        from.writeText(
            from.readText()
                .replace("version\\s*=\\s*'(.+)'".toRegex(), "version = '${project.version}'")
        )
    }
}

nexusPublishing {
    val localProps = gradleLocalProperties(rootDir)
    val ossrhUsername = localProps.getProperty("ossrhUsername", System.getenv("OSSRH_USERNAME"))
    val ossrhPassword = localProps.getProperty("ossrhPassword", System.getenv("OSSRH_PASSWORD"))
    val sonatypeStagingProfileId = localProps.getProperty(
        "sonatypeStagingProfileId",
        System.getenv("SONATYPE_STAGING_PROFILE_ID")
    )

    repositories {
        sonatype {
            stagingProfileId.set(sonatypeStagingProfileId)
            username.set(ossrhUsername)
            password.set(ossrhPassword)
            nexusUrl.set(uri("https://s01.oss.sonatype.org/service/local/"))
            snapshotRepositoryUrl.set(uri("https://s01.oss.sonatype.org/content/repositories/snapshots/"))
        }
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
    val localProps = gradleLocalProperties(rootDir)
    val signingKey = localProps.getProperty("signing.key", System.getenv("SIGNING_KEY"))
    val signingPassword = localProps.getProperty(
        "signing.password",
        System.getenv("SIGNING_PASSWORD")
    )
    useInMemoryPgpKeys(signingKey, signingPassword)
    sign(publishing.publications)
}

