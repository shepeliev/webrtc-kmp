package com.shepeliev.webrtckmp

actual fun setupMocks() {
    WebRtc.configureBuilder {
        audioModuleBuilder = { null }
    }
}
