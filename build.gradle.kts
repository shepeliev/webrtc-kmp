buildscript {
    repositories {
        google()
        mavenCentral()
    }

    dependencies {
        classpath(deps.kotlin.gradlePlugin)
        classpath(deps.kotlin.serializationPlugin)
        classpath(deps.android.gradlePlugin)
        classpath(deps.google.servicesGradlePlugin)
    }
}

allprojects {
    repositories {
        mavenLocal()
        google()
        mavenCentral()
    }

    tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>() {
        kotlinOptions {
            freeCompilerArgs = freeCompilerArgs + "-opt-in=kotlin.RequiresOptIn"
        }
    }
}
