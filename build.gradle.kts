import com.android.build.gradle.internal.cxx.configure.gradleLocalProperties

buildscript {
    val kotlinVersion by extra("1.4.31")
    val navVersion by extra("2.3.3")

    repositories {
        gradlePluginPortal()
        jcenter()
        google()
        mavenCentral()
    }
    dependencies {
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlinVersion")
        classpath("com.android.tools.build:gradle:4.1.2")
        classpath("androidx.navigation:navigation-safe-args-gradle-plugin:$navVersion")
    }
}

// TODO: Hierarchical project structures are not fully supported in IDEA, enable only for a regular built (https://youtrack.jetbrains.com/issue/KT-35011)
// add idea.active=true for local development
val _ideaActive = gradleLocalProperties(rootDir)["idea.active"] == "true"

allprojects {
    repositories {
        google()
        jcenter()
        mavenCentral()
    }
}

subprojects {
    val ideaActive by extra(_ideaActive)
    println("Is IDEA active: $ideaActive")

    tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().all {
        kotlinOptions {
            jvmTarget = "1.8"
        }
    }
}
