package com.shepeliev.webrtckmp

expect class RtpCapabilities {
    val codecs: List<CodecCapability>
    val headerExtensions: List<HeaderExtensionCapability>
    class CodecCapability {
        val preferredPayloadType: Int?
        val name: String?
        val kind: MediaStreamTrackKind
        val clockRate: Int
        val numChannels: Int?
        val parameters: Map<String, String>
        val mimeType: String
    }
    class HeaderExtensionCapability {
        val uri: String
        val preferredId: Int?
        val preferredEncrypted: Boolean?
    }
}
