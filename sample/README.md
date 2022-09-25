# WebRTC KMP sample app

Demo application for WebRTC KMP.

## Build and start

```bash
git clone https://github.com/shepeliev/webrtc-kmp.git
cd webrtc-kmp
carthage update --use--xcframeworks
```

### Android

Run Android emulator or connect real device.

```bash
./graldew installDebug
```

### iOS

```bash
./gradlew assembleSharedDebugXCFramework
```

Then open `sample/app-ios/app-ios.xcodeproj` in XCode build and run

### Web

```bash
./gradlew browserRun
```
