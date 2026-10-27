# WebRTC KMP sample app

Demo application for WebRTC KMP.

## Build and start

```bash
git clone https://github.com/shepeliev/webrtc-kmp.git
cd webrtc-kmp
```

### Android

Run Android emulator or connect real device.

```bash
./gradlew :sample:androidApp:installDebug
```

### iOS
In `sample/iosApp/Configuration/Config.xcconfig` set `TEAM_ID` and `BUNDLE_ID` to your values.

Open `sample/iosApp/iosApp.xcodeproj` in Xcode, build and run. The Kotlin framework and the WebRTC Swift package
are built and linked by the "Compile Kotlin Framework" build phase and `KotlinMultiplatformLinkedPackage`.

### Web JS

```bash
./gradlew sample:composeApp:jsBrowserDevelopmentRun 
```

### Web WasmJS

```bash
./gradlew sample:composeApp:wasmJsBrowserDevelopmentRun 
```
