buildscript {
    repositories {
        google()
        mavenCentral()
    }

    dependencies {
        classpath(deps.kotlin.gradlePlugin)
        classpath(deps.android.gradlePlugin)
    }
}

allprojects {
    repositories {
        mavenLocal()
        google()
        mavenCentral()
    }
}
