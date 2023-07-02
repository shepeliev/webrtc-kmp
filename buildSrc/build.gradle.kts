plugins {
    `kotlin-dsl`
}

repositories {
    gradlePluginPortal()
    mavenCentral()
    google()
}

dependencies {
    implementation("com.android.tools.build:gradle:8.0.0")
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:1.8.21")
    implementation("org.jlleitschuh.gradle:ktlint-gradle:10.3.0")
    implementation("de.undercouch:gradle-download-task:5.1.0")
}
