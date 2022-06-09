buildscript {
    repositories {
        google()
        mavenCentral()
    }

    dependencies {
        classpath(":build-plugins")
        classpath("org.jetbrains.kotlin:kotlin-serialization:1.6.10")
        classpath("com.google.gms:google-services:4.3.10")
    }
}

allprojects {
    tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>() {
        kotlinOptions {
            freeCompilerArgs = freeCompilerArgs + "-opt-in=kotlin.RequiresOptIn"
        }
    }
}
