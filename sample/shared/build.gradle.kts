plugins {
    id("multiplatform-setup")
}

dependencies {
    commonMainApi(deps.decompose)
    commonMainImplementation(deps.firebase.firestore)
    androidMainImplementation(deps.android.core)
}
