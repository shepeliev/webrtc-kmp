plugins {
    kotlin("js")
}

kotlin {
    js(IR) {
        browser()
        binaries.executable()
    }
}

dependencies{
    implementation(project(":sample:shared"))
    implementation(project.dependencies.enforcedPlatform(deps.kotlin.wrappers.bom))
    implementation(deps.kotlin.wrappers.emotion)
    implementation(deps.kotlin.wrappers.react)
    implementation(deps.kotlin.wrappers.reactDom)
    implementation(deps.kotlin.wrappers.mui)
}
