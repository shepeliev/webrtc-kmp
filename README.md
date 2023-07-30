# WebRTC KMP ![Maven Central](https://img.shields.io/maven-central/v/com.shepeliev/webrtc-kmp?style=flat-square)

WebRTC Kotlin Multiplatform SDK

## API implementation map
 API | Android | iOS | JS 
 :-: | :-----: | :-: | :---: 
 Audio/Video |  :white_check_mark: | :white_check_mark: | :white_check_mark:
 Data channel | :white_check_mark: | :white_check_mark: | :white_check_mark:
 Screen Capture | | | :white_check_mark:

## WebRTC revision
Current revision: M114

## Installation
The library is published to [Maven Central](https://search.maven.org/artifact/com.shepeliev/webrtc-kmp).

Shared module build.gradle.kts
```Kotlin
kotlin {
  cocoapods {
    version = "1.0.0"
    summary = "Shared module"
    homepage = "not published"
    ios.deploymentTarget = "10.0"

    specRepos {
      url("https://github.com/webrtc-sdk/Specs.git")
    }
   
    pod("WebRTC-SDK") {
      version = "114.5735.02"
      linkOnly = true
    }
  
    podfile = project.file("../iosApp/Podfile")
  
    framework {
      baseName = "shared"
      export(project(":webrtc-kmp"))
      transitiveExport = true
    }
  
    xcodeConfigurationToNativeBuildType["CUSTOM_DEBUG"] = NativeBuildType.DEBUG
    xcodeConfigurationToNativeBuildType["CUSTOM_RELEASE"] = NativeBuildType.RELEASE
  }

 
  android()
  
  ios()
 
  js {
   useCommonJs()
   browser()
  }
  
  sourceSets {
      val commonMain by getting {
          dependencies {
              api("com.shepeliev:webrtc-kmp:$webRtcKmpVersion")
          }
      }
  }
}
```

Also add the following to your `Podfile` in the target section:
```Ruby
use_frameworks!
pod 'shared', :path => '../shared'
```

## Usage

Please reffer to [sample](sample/README.md).
