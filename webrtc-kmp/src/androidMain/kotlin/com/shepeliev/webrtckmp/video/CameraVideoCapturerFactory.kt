package com.shepeliev.webrtckmp.video

import org.webrtc.CameraEnumerator
import org.webrtc.CameraVideoCapturer

/**
 * Factory for creating [CameraVideoCapturer] instances.
 */
fun interface CameraVideoCapturerFactory {

    /**
     * Creates a [CameraVideoCapturer] for the specified device.
     *
     * @param deviceId the device ID of the camera to use.
     * @param enumerator the [CameraEnumerator] to use for creating the capturer.
     * @param eventsHandler the handler for camera events.
     * @return the created [CameraVideoCapturer] or `null` if the capturer can't be created.
     */
    fun createCameraVideoCapturer(
        deviceId: String?,
        enumerator: CameraEnumerator,
        eventsHandler: CameraVideoCapturer.CameraEventsHandler?
    ): CameraVideoCapturer?
}

/**
 * Default implementation of [CameraVideoCapturerFactory].
 */
fun CameraVideoCapturerFactory(): CameraVideoCapturerFactory {
    return CameraVideoCapturerFactory { deviceId, enumerator, eventsHandler ->
        deviceId?.let { enumerator.createCapturer(it, eventsHandler) }
    }
}
