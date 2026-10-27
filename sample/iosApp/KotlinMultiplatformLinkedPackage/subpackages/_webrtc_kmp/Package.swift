// swift-tools-version: 5.9
import PackageDescription
let package = Package(
  name: "_webrtc_kmp",
  platforms: [
    .iOS("15.0")
  ],
  products: [
    .library(
      name: "_webrtc_kmp",
      type: .none,
      targets: ["_webrtc_kmp"]
    )
  ],
  dependencies: [
    .package(
      url: "https://github.com/webrtc-sdk/Specs.git",
      exact: "137.7151.00"
    )
  ],
  targets: [
    .target(
      name: "_webrtc_kmp",
      dependencies: [
        .product(
          name: "WebRTC",
          package: "Specs"
        )
      ]
    )
  ]
)
