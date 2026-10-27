# WebRTC KMP ![Maven Central](https://img.shields.io/maven-central/v/com.shepeliev/webrtc-kmp?style=flat-square)

WebRTC Kotlin Multiplatform SDK is a comprehensive toolkit for integrating WebRTC functionality into your multiplatform projects. 
It supports Android, iOS, JS. Other platforms - PRs are welcome.


## API implementation map
 API |      Android       | iOS | JS/WasmJS 
 :-: |:------------------:| :-: | :---: 
 Audio/Video | :white_check_mark: | :white_check_mark: | :white_check_mark:
 Data channel | :white_check_mark: | :white_check_mark: | :white_check_mark:
 Screen Capture | :white_check_mark: | | :white_check_mark:

## WebRTC revision
Current revision: M137

## Installation
The library is published to [Maven Central](https://search.maven.org/artifact/com.shepeliev/webrtc-kmp).


### Add dependency to your common source set:
```kotlin
commonMain.dependencies {
  dependencies {
    implementation("com.shepeliev:webrtc-kmp:$webRtcKmpVersion")
  }
}
```

### Running on iOS
On iOS, the WebRTC SDK ([WebRTC-SDK](https://github.com/webrtc-sdk/Specs) Swift package) comes as a transitive
dependency through Kotlin's [SwiftPM import](https://kotl.in/cocoapods-to-swiftpm-migration), so you don't add it
to your iOS project yourself. This requires:

- Kotlin 2.4.0 or later
- iOS deployment target 15.0 or later
- No `kotlin("native.cocoapods")` plugin in the module that builds your iOS framework. The Kotlin Gradle plugin
  can't combine CocoaPods with SwiftPM dependencies, including transitive ones. If you still use it, migrate first;
  see the [migration guide](https://kotl.in/cocoapods-to-swiftpm-migration).

Integrate the Xcode project once by running the integration tasks for the module that builds your iOS framework
(e.g. `:shared`):

```bash
XCODEPROJ_PATH="$PWD/iosApp/iosApp.xcodeproj" GRADLE_PROJECT_PATH=":shared" \
  ./gradlew :shared:integrateEmbedAndSign :shared:integrateLinkagePackage
```

This adds a build phase that runs `embedAndSignAppleFrameworkForXcode` and a local `KotlinMultiplatformLinkedPackage`
that links the Swift packages into your app. If you skip it, building in Xcode fails with an error that prints this
command for your project. Also set _User Script Sandboxing_ (`ENABLE_USER_SCRIPT_SANDBOXING`) to `NO` for your app
target.

## Usage

Please refer to [sample](sample/README.md).

### Screen Share in Android
```kotlin
// Set MediaProjection permission intent using `MediaProjectionIntentHolder`
val mediaProjectionPermissionLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.StartActivityForResult()
) { activityResult ->
    activityResult.data?.also {
        MediaProjectionIntentHolder.intent = it
    }
}
```
