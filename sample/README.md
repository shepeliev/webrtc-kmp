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
./gradlew installDebug
```

### iOS
In `sample/iosApp/Configuration/Config.xcconfig` set `TEAM_ID` and `BUNDLE_ID` to your values.

```bash
./gradlew generateDummyFramework
cd sample/iosApp
pod install
```

Open `sample/iosApp/iosApp.xcworkspace` in XCode build and run

### Web JS

```bash
./gradlew sample:composeApp:jsBrowserDevelopmentRun 
```

### Web WasmJS

```bash
./gradlew sample:composeApp:wasmJsBrowserDevelopmentRun 
```
