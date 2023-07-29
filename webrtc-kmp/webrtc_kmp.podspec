Pod::Spec.new do |spec|
    spec.name                     = 'webrtc_kmp'
    spec.version                  = '0.114.2'
    spec.homepage                 = 'https://github.com/shepeliev/webrtc-kmp'
    spec.source                   = { :http=> ''}
    spec.authors                  = ''
    spec.license                  = ''
    spec.summary                  = 'WebRTC Kotlin Multiplatform SDK'
    spec.vendored_frameworks      = 'build/cocoapods/framework/webrtc_kmp.framework'
    spec.libraries                = 'c++'
    spec.ios.deployment_target = '10.0'
    spec.dependency 'WebRTC-SDK', '114.5735.02'
                
    spec.pod_target_xcconfig = {
        'KOTLIN_PROJECT_PATH' => ':webrtc-kmp',
        'PRODUCT_MODULE_NAME' => 'webrtc_kmp',
    }
                
    spec.script_phases = [
        {
            :name => 'Build webrtc_kmp',
            :execution_position => :before_compile,
            :shell_path => '/bin/sh',
            :script => <<-SCRIPT
                if [ "YES" = "$OVERRIDE_KOTLIN_BUILD_IDE_SUPPORTED" ]; then
                  echo "Skipping Gradle build task invocation due to OVERRIDE_KOTLIN_BUILD_IDE_SUPPORTED environment variable set to \"YES\""
                  exit 0
                fi
                set -ev
                REPO_ROOT="$PODS_TARGET_SRCROOT"
                "$REPO_ROOT/../gradlew" -p "$REPO_ROOT" $KOTLIN_PROJECT_PATH:syncFramework \
                    -Pkotlin.native.cocoapods.platform=$PLATFORM_NAME \
                    -Pkotlin.native.cocoapods.archs="$ARCHS" \
                    -Pkotlin.native.cocoapods.configuration="$CONFIGURATION"
            SCRIPT
        }
    ]
                
end