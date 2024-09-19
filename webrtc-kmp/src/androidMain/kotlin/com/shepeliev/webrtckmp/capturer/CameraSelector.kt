package com.shepeliev.webrtckmp.capturer

import com.shepeliev.webrtckmp.FacingMode
import com.shepeliev.webrtckmp.MediaTrackConstraints
import com.shepeliev.webrtckmp.value
import org.webrtc.CameraEnumerator

/**
 * Selects a video capture device based on the provided constraints.
 */
fun interface CameraSelector {
    /**
     * Selects a video capture device based on the provided constraints.
     *
     * @param enumerator the camera enumerator to use for device selection
     * @param constraints the constraints to use for device selection
     * @return the device ID of the selected camera or `null` if no device was found
     */
    fun selectCameraId(enumerator: CameraEnumerator, constraints: MediaTrackConstraints): String?
}

/**
 * Default implementation of [CameraSelector].
 */
fun CameraSelector(): CameraSelector = CameraSelector { enumerator, constraints ->
    val isFrontFacing = constraints.facingMode?.value != FacingMode.Environment

    val searchCriteria: (String) -> Boolean = if (constraints.deviceId != null) {
        { it == constraints.deviceId }
    } else {
        { enumerator.isFrontFacing(it) == isFrontFacing }
    }

    enumerator.deviceNames.firstOrNull(searchCriteria)
}
