package com.shepeliev.webrtckmp

expect class RtpTransceiver {
    /**
     * Indicates the transceiver's current directionality, or null if the transceiver is stopped or
     * has never participated in an exchange of offers and answers. To change the transceiver's
     * directionality, set the value of the direction property.
     */
    val currentDirection: RtpTransceiverDirection?

    /**
     *The transceiver's desired direction.
     */
    var direction: RtpTransceiverDirection

    /**
     * The media ID of the m-line associated with this transceiver. This association is established,
     * when possible, whenever either a local or remote description is applied. This field is null
     * if neither a local or remote description has been applied, or if its associated m-line is
     * rejected by either a remote offer or any answer.
     */
    val mid: String

    /**
     * The [RtpReceiver] that handles receiving and decoding incoming media.
     */
    val receiver: RtpReceiver

    /**
     * The [RtpSender] responsible for encoding and sending data to the remote peer.
     */
    val sender: RtpSender

    /**
     * Indicates whether or not sending and receiving using the paired [RtpSender] and
     * [RtpReceiver] has been permanently disabled, either due to SDP offer/answer, or due to a call
     * to [stop].
     */
    val stopped: Boolean

    fun setCodecPreferences(capabilities: List<RtpCapabilities.CodecCapability>)

    /**
     * Permanently stops the [RtpTransceiver]. The associated sender stops sending data, and the
     * associated receiver likewise stops receiving and decoding incoming data.
     */
    fun stop()
}

enum class RtpTransceiverDirection {
    SendRecv, SendOnly, RecvOnly, Inactive, Stopped;

    fun toCanonicalForm(): String = name.lowercase()
}
