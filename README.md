# WebRTC KMP ![Maven Central](https://img.shields.io/maven-central/v/com.shepeliev/webrtc-kmp?style=flat-square)

WebRTC Kotlin Multiplatform SDK

## API implementation map
 API | Android | iOS | JS 
 :-: | :-----: | :-: | :---: 
 Audio/Video |  :white_check_mark: | :white_check_mark: | :white_check_mark:
 Data channel | :white_check_mark: | :white_check_mark: | :white_check_mark:
 Screen Capture | | | :white_check_mark:

## WebRTC revision
Current revision: M89

## Installation
Root build.gradle.kts

```Kotlin
allprojects {
    repositories {
        mavenCentral()
    }
}
```

Shared module build.gradle.kts
```Kotlin
kotlin {

  ios {
      binaries
          .filterIsInstance<Framework>()
          .forEach {
              it.transitiveExport = true
              it.export("com.shepeliev:webrtc-kmp:$webRtcKmmVersion")
          }
  }

  sourceSets {
      val commonMain by getting {
          dependencies {
              api("com.shepeliev:webrtc-kmp:$webRtcKmmVersion")
              implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.5.2-native-mt")
          }
      }
  }
}
```

On iOS, in addition to the Kotlin library add in Podfile
```
pod 'webrtc-kmp', :git => 'git@github.com:shepeliev/webrtc-kmp.git'
```

## Usage

Please reffer to [sample project](https://github.com/shepeliev/webrtc-kmp-demo).
