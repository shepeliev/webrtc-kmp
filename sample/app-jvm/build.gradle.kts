import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    kotlin("multiplatform")
    id("org.jetbrains.compose")
}

kotlin {
    jvm {
        compilations.all {
            kotlinOptions.jvmTarget = "17"
        }
        withJava()
    }
    sourceSets {
        val jvmMain by getting {
            dependencies {
                implementation(project(":sample:shared"))
                implementation(compose.material)
                implementation(compose.desktop.currentOs)
                implementation(deps.decompose.kmp)
                implementation(deps.kotlin.coroutines.swing)
            }
        }
        val jvmTest by getting
    }
}

compose.desktop {
    application {
        mainClass = "com.shepeliev.webrtckmp.MainKt"
        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "KMPTemplate"
            packageVersion = "1.0.0"
        }
    }
}
