# WebRTC KMP ![Maven Central](https://img.shields.io/maven-central/v/com.shepeliev/webrtc-kmp?style=flat-square)

WebRTC Kotlin Multiplatform SDK

## API implementation map
 API | Android | iOS | JS 
 :-: | :-----: | :-: | :---: 
 Audio/Video |  :white_check_mark: | :white_check_mark: | :white_check_mark:
 Data channel | :white_check_mark: | :white_check_mark: | :white_check_mark:
 Screen Capture | | | :white_check_mark:

## WebRTC revision
Current revision: M106

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
              implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")
          }
      }
  }
}
```

## Usage

Please reffer to [sample](sample/README.md).
