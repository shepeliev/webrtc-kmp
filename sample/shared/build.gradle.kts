plugins {
    id("multiplatform-setup")
    kotlin("plugin.serialization")
}

dependencies {
    commonMainApi(project(":webrtc-kmp"))
    commonMainApi(deps.decompose)
    commonMainImplementation(deps.kotlin.coroutines)
    commonMainImplementation(deps.kotlin.serialization.json)
    commonMainImplementation(deps.firebase.firestore)
    commonMainImplementation(deps.kermit)
}
