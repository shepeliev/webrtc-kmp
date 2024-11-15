# WebRTC KMP ![Maven Central](https://img.shields.io/maven-central/v/com.shepeliev/webrtc-kmp?style=flat-square)

WebRTC Kotlin Multiplatform SDK is a comprehensive toolkit for integrating WebRTC functionality into your multiplatform projects. 
It supports Android, iOS, JS. Other platforms - PRs are welcome.


## API implementation map
|      API       |      Android       |        iOS         |     JS/WasmJS      |        JVM         |
|:--------------:|:------------------:|:------------------:|:------------------:|:------------------:|
|  Audio/Video   | :white_check_mark: | :white_check_mark: | :white_check_mark: | :white_check_mark: |
|  Data channel  | :white_check_mark: | :white_check_mark: | :white_check_mark: | :white_check_mark: |
| Screen Capture | :white_check_mark: |                    | :white_check_mark: | :white_check_mark: |

## WebRTC revision
Current revision: M125

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
On iOS, the WebRTC SDK is not linked as a transitive dependency, so you need to add it to your iOS project manually.
This can be done using CocoaPods or SPM, depending on your project setup. Here is an example of how to link 
the WebRTC SDK using CocoaPods in `build.gradle.kts`:

```kotlin
kotlin {
    cocoapods {
        version = "1.0.0"
        summary = "Shared module"
        homepage = "not published"
        ios.deploymentTarget = "13.0"

        pod("WebRTC-SDK") {
              version = "125.6422.05"
              moduleName = "WebRTC"
        }

        podfile = project.file("../iosApp/Podfile")

        framework {
            baseName = "shared"
            isStatic = true  
        }

        xcodeConfigurationToNativeBuildType["CUSTOM_DEBUG"] = NativeBuildType.DEBUG
        xcodeConfigurationToNativeBuildType["CUSTOM_RELEASE"] = NativeBuildType.RELEASE
    }

    iosX64()
    iosArm64()
    iosSimulatorArm64()
    jvm()

}

dependencies {
    // Choose the right architecture for your system
    // see https://github.com/devopvoid/webrtc-java for supported platforms
    val osName = System.getProperty("os.name").lowercase()
    val hostOS = if (osName.contains("mac")) {
        "macos"
    } else if (osName.contains("linux")) {
        "linux"
    } else if (osName.contains("windows")) {
        "windows"
    } else {
        throw IllegalStateException("Unsupported OS: $osName")
    }
    val hostArch = when (val arch = System.getProperty("os.arch").lowercase()) {
        "amd64" -> "x86_64"
        else -> arch
    }
    jvmMainImplementation(
        group = "dev.onvoid.webrtc",
        name = "webrtc-java",
        version = "0.8.0",
        classifier = "$hostOS-$hostArch"
    )
}
```

Also add the following to your `Podfile` in the target section:
```Ruby
use_frameworks!
pod 'shared', :path => '../shared'
```

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
