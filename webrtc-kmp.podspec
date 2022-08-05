version = '0.89.7'

Pod::Spec.new do |s|
  s.name                = 'webrtc-kmp'
  s.version             = version
  s.summary             = 'WebRTC Kotlin Multiplatform'
  s.homepage            = 'https://github.com/shepeliev/webrtc-kmp'
  s.license             = 'Apache 2.0'
  s.author              = 'https://github.com/shepeliev/webrtc-kmp/graphs/contributors'
  s.source              = { :git => 'git@github.com:shepeliev/webrtc-kmp.git', :tag => "v#{s.version}" }
  s.requires_arc        = true
  s.platforms           = { :ios => '11.0', :osx => '10.13' }
  s.libraries           = 'c', 'sqlite3', 'stdc++'
  s.framework           = 'AudioToolbox','AVFoundation', 'CoreAudio', 'CoreGraphics', 'CoreVideo', 'GLKit', 'VideoToolbox'
  s.ios.vendored_frameworks   = 'vendor/apple/WebRTC.xcframework'
  s.macos.vendored_frameworks = 'vendor/apple/WebRTC.xcframework'
end
