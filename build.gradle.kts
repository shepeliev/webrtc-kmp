import java.util.Properties

plugins {
    alias(libs.plugins.ktlint)
    alias(libs.plugins.nexus)
    alias(libs.plugins.kotlinMultiplatform) apply false
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.androidLibrary) apply false
    alias(libs.plugins.jetbrains.compose) apply false
}

val localProps = Properties()
val localPropertiesFile = rootProject.file("local.properties")
if (localPropertiesFile.exists()) {
    localPropertiesFile.inputStream().use { localProps.load(it) }
}

val signingKey by extra(
    localProps.getOrDefault("signing.key", System.getenv("SIGNING_KEY") ?: "")
)
val signingPassword by extra(
    localProps.getOrDefault("signing.password", System.getenv("SIGNING_PASSWORD") ?: "")
)
val ossrhUsername by extra(
    localProps.getOrDefault("ossrhUsername", System.getenv("OSSRH_USERNAME") ?: "")
)
val ossrhPassword by extra(
    localProps.getOrDefault("ossrhPassword", System.getenv("OSSRH_PASSWORD") ?: "")
)
val sonatypeStagingProfileId by extra(
    localProps.getOrDefault(
        "sonatypeStagingProfileId",
        System.getenv("SONATYPE_STAGING_PROFILE_ID") ?: ""
    )
)

nexusPublishing {
    this.repositories {
        sonatype {
            val sonatypeStagingProfileId: String by rootProject.extra
            val ossrhUsername: String by rootProject.extra
            val ossrhPassword: String by rootProject.extra

            stagingProfileId.set(sonatypeStagingProfileId)
            username.set(ossrhUsername)
            password.set(ossrhPassword)
            nexusUrl.set(uri("https://s01.oss.sonatype.org/service/local/"))
            snapshotRepositoryUrl.set(uri("https://s01.oss.sonatype.org/content/repositories/snapshots/"))
        }
    }
}
